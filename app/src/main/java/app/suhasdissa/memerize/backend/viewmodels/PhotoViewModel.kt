/*******************************************************************************
Created By Suhas Dissanayake on 8/3/23, 12:11 PM
Copyright (c) 2023
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.backend.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.suhasdissa.memerize.BuildConfig
import app.suhasdissa.memerize.utils.SaveDirectoryKey
import app.suhasdissa.memerize.utils.preferences
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoViewModel : ViewModel() {

    var downloadState: DownloadState by mutableStateOf(DownloadState.NotStarted)

    private suspend fun getBitmapFromUrl(url: String, context: Context): Bitmap? {
        val imageLoader = ImageLoader.Builder(context).build()
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        val result = imageLoader.execute(request)

        if (result is SuccessResult) {
            return result.drawable.toBitmap()
        }
        return null
    }

    fun savePhotoToDisk(url: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                downloadState = DownloadState.Loading
            }
            val bitmap = getBitmapFromUrl(url, context)
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
            val outputFile = saveDir.createFile("image/jpg", "${UUID.randomUUID()}.jpg")
            if (bitmap != null) {
                try {
                    val outputStream = context.contentResolver.openOutputStream(outputFile!!.uri)!!
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    withContext(Dispatchers.Main) {
                        downloadState = DownloadState.Success
                    }
                } catch (e: Exception) {
                    Log.e("Photo save", e.toString())
                    withContext(Dispatchers.Main) {
                        downloadState = DownloadState.Error
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    downloadState = DownloadState.Error
                }
            }
        }
    }

    fun shareImage(url: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = getBitmapFromUrl(url, context)
            if (bitmap != null) {
                try {
                    val outputFile = File(
                        context.cacheDir,
                        "${UUID.randomUUID()}.jpg"
                    )
                    val outputStream = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".provider",
                                outputFile
                            )
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        type = "image/jpg"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Send Photo to..")
                    context.startActivity(shareIntent)
                } catch (e: Exception) {
                    Log.e("Share Image", e.toString())
                }
            }
        }
    }
}

sealed interface DownloadState {
    object NotStarted : DownloadState
    object Success : DownloadState
    object Error : DownloadState
    object Loading : DownloadState
}
