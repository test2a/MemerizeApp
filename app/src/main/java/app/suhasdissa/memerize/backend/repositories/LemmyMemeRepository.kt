/*******************************************************************************
Created By Suhas Dissanayake on 8/4/23, 9:06 PM
Copyright (c) 2023
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.backend.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import app.suhasdissa.memerize.backend.apis.LemmyApi
import app.suhasdissa.memerize.backend.database.dao.LemmyMemeDAO
import app.suhasdissa.memerize.backend.database.entity.LemmyCommunity
import app.suhasdissa.memerize.backend.database.entity.LemmyMeme

class LemmyMemeRepository(
    private val lemmyDAO: LemmyMemeDAO,
    private val lemmyApi: LemmyApi
) : MemeRepository<LemmyMeme, LemmyCommunity> {

    override suspend fun getOnlineData(
        community: LemmyCommunity,
        time: String
    ): List<LemmyMeme>? {
        return try {
            val memesList = getNetworkData(community, time)
            Thread {
                insertMemes(memesList)
            }.start()
            memesList
        } catch (e: Exception) {
            Log.e("Lemmy Repository", e.toString())
            null
        }
    }

    private suspend fun getNetworkData(
        community: LemmyCommunity,
        time: String
    ): List<LemmyMeme> {
        val memeList: ArrayList<LemmyMeme> = arrayListOf()
        val lemmyData = lemmyApi.getLemmyData(
            instance = community.instance,
            community = community.id,
            sort = time
        ).posts
        lemmyData.forEach { post ->
            val url = post.post?.url ?: ""
            val title = post.post?.name ?: ""
            if (url.endsWith("jpg") || url.endsWith("jpeg") || url.endsWith("png")) {
                val id = url.hashCode().toString()
                memeList.add(
                    LemmyMeme(
                        id,
                        url,
                        title,
                        false,
                        url,
                        community.name,
                        community.instance
                    )
                )
            }
        }
        return memeList
    }

    override suspend fun getLocalData(community: LemmyCommunity): List<LemmyMeme> =
        lemmyDAO.getAll(community.id, community.instance)

    @WorkerThread
    private fun insertMemes(memes: List<LemmyMeme>) {
        lemmyDAO.insertAll(memes)
    }
}
