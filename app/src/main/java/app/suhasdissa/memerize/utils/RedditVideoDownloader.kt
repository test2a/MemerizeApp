package app.suhasdissa.memerize.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import app.suhasdissa.memerize.utils.InAppLogger
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
// removed duplicate/incorrect import
import app.suhasdissa.memerize.backend.apis.FileDownloadApi
// removed duplicate/incorrect import
import app.suhasdissa.memerize.backend.apis.RedditVideoApi
import app.suhasdissa.memerize.utils.preferences
import app.suhasdissa.memerize.utils.SaveDirectoryKey
import java.nio.ByteBuffer
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RedditVideoDownloader {

    private val xmlRetrofit = Retrofit.Builder()
        .baseUrl("https://www.reddit.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val downloadRetrofit = Retrofit.Builder()
        .baseUrl("https://v.redd.it/")
        .build()

    private val apiService: RedditVideoApi = xmlRetrofit.create(RedditVideoApi::class.java)

    private val fileDownloadApiService: FileDownloadApi =
        downloadRetrofit.create(FileDownloadApi::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Recycle")
    suspend fun downloadRedditVideo(
        context: Context,
        url: String,
        outputFileName: String
    ): Boolean {
        val urlS = getRedditUrls(url) ?: return false
            val redditUrl = Regex("https?://v\\.redd\\.it/\\S+/").find(url)?.value ?: return false

        return withContext(Dispatchers.IO) {
            val files = listOfNotNull(
                async { downloadFile(redditUrl + urlS.first, context) },
                urlS.second?.let { async { downloadFile(redditUrl + it, context) } }
            )
            val result = files.awaitAll()

            val videoFile = result.getOrNull(0) ?: return@withContext false
            val audioFile = result.getOrNull(1)

            val outputFile = getOutputFile(outputFileName, context) ?: return@withContext false
            var videoPfd: ParcelFileDescriptor? = null
            var audioPfd: ParcelFileDescriptor? = null
            var outPfd: ParcelFileDescriptor? = null
            try {
                videoPfd = ParcelFileDescriptor.open(videoFile, ParcelFileDescriptor.MODE_READ_ONLY)
                audioPfd = audioFile?.let { ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY) }
                outPfd = context.contentResolver.openFileDescriptor(outputFile.uri, "w")
                if (outPfd == null) return@withContext false
                val success = muxVideoAndAudio(videoPfd, audioPfd, outPfd)
                success
            } finally {
                try { videoPfd?.close() } catch (_: Exception) {}
                try { audioPfd?.close() } catch (_: Exception) {}
                try { outPfd?.close() } catch (_: Exception) {}
            }
        }
    }

    /**
     * https://github.com/Docile-Alligator/Infinity-For-Reddit/blob/d0a9d9af9a46477a9bc1ff36af11278fcba06aa5/app/src/main/java/ml/docilealligator/infinityforreddit/services/DownloadRedditVideoService.java#L348C7-L426C10
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")
    private fun muxVideoAndAudio(
        videoPfd: ParcelFileDescriptor,
        audioPfd: ParcelFileDescriptor?,
        pfd: ParcelFileDescriptor
    ): Boolean {
        try {
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoPfd.fileDescriptor)
            val muxer = MediaMuxer(pfd.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            videoExtractor.selectTrack(0)
            val videoFormat = videoExtractor.getTrackFormat(0)
            val videoTrack = muxer.addTrack(videoFormat)
            var sawEOS = false
            val offset = 100
            val sampleSize = 4096 * 1024
            val videoBuf = ByteBuffer.allocate(sampleSize)
            val audioBuf = ByteBuffer.allocate(sampleSize)
            val videoBufferInfo = MediaCodec.BufferInfo()
            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            // audio not present for all videos
            val audioExtractor = MediaExtractor()
            val audioBufferInfo = MediaCodec.BufferInfo()
            var audioTrack = -1
            if (audioPfd != null) {
                audioExtractor.setDataSource(audioPfd.fileDescriptor)
                audioExtractor.selectTrack(0)
                val audioFormat = audioExtractor.getTrackFormat(0)
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                audioTrack = muxer.addTrack(audioFormat)
            }
            muxer.start()
            while (!sawEOS) {
                videoBufferInfo.offset = offset
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset)
                if (videoBufferInfo.size < 0) {
                    sawEOS = true
                    videoBufferInfo.size = 0
                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.sampleTime
                    videoBufferInfo.flags = videoExtractor.sampleFlags
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo)
                    videoExtractor.advance()
                }
            }
            if (audioPfd != null) {
                var sawEOS2 = false
                while (!sawEOS2) {
                    audioBufferInfo.offset = offset
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset)
                    if (audioBufferInfo.size < 0) {
                        sawEOS2 = true
                        audioBufferInfo.size = 0
                    } else {
                        audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                        audioBufferInfo.flags = audioExtractor.sampleFlags
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo)
                        audioExtractor.advance()
                    }
                }
            }
            muxer.stop()
            muxer.release()
        } catch (e: Exception) {
            Log.e("Video Muxer", e.message, e)
            InAppLogger.log("Video Muxer: ${e.message}\n${e.stackTraceToString()}")
            return false
        }
        return true
    }

    private suspend fun downloadFile(url: String, context: Context): File? {
        return withContext(Dispatchers.IO) {
            try {
                val call = fileDownloadApiService.downloadFile(url)
                val response = call.execute()
                if (response.isSuccessful) {
                    val body = response.body() ?: return@withContext null
                    val inputStream = BufferedInputStream(body.byteStream())
                    val tempFile = getTempFile(context)
                    val outputStream = BufferedOutputStream(FileOutputStream(tempFile))
                    try {
                        val buffer = ByteArray(8 * 1024)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                        outputStream.flush()
                    } finally {
                        try { inputStream.close() } catch (_: Exception) {}
                        try { outputStream.close() } catch (_: Exception) {}
                    }
                    tempFile
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("File Download", e.message, e)
                InAppLogger.log("File Download: ${e.message}\n${e.stackTraceToString()}")
                null
            }
        }
    }

    private suspend fun getRedditUrls(url: String): Pair<String, String?>? {
        return try {
            val text = apiService.getRedditData(url)
            return matchRedditUrls(text)
        } catch (e: Exception) {
            Log.e("Reddit Urls", e.message, e)
            InAppLogger.log("Reddit Urls: ${e.message}\n${e.stackTraceToString()}")
            null
        }
    }

    private fun matchRedditUrls(text: String): Pair<String, String?>? {
        val regex = Regex("<BaseURL>(DASH(_AUDIO)?_\\d+\\.\\S+)</BaseURL>")
        val matcher = regex.findAll(text)

        val video = mutableListOf<String?>()
        val audio = mutableListOf<String?>()

        for (matchResult in matcher) {
            val match = matchResult.groups[1]?.value
            val isAudio = matchResult.groups[2]?.value

            if (isAudio != null) {
                audio.add(match)
            } else {
                video.add(match)
            }
        }

        val selectedVideo = video.takeIf { it.isNotEmpty() }?.last() ?: return null
        val selectedAudio = audio.takeIf { it.isNotEmpty() }?.last()

        return selectedVideo to selectedAudio
    }

    private suspend fun getOutputFile(fileName: String, context: Context): DocumentFile? {
        return withContext(Dispatchers.IO) {
            val prefDir =
                context.preferences.getString(SaveDirectoryKey, null)

            val saveDir = when {
                prefDir.isNullOrBlank() -> {
                    val dir =
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                    DocumentFile.fromFile(dir)
                }

                else -> DocumentFile.fromTreeUri(context, Uri.parse(prefDir))!!
            }

            saveDir.createFile("video/mp4", fileName)
        }
    }

    private suspend fun getTempFile(context: Context): File {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "${UUID.randomUUID()}.mp4")
            if (!file.exists()) file.createNewFile()
            file
        }
    }
}
