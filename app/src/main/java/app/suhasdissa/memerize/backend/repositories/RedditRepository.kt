package app.suhasdissa.memerize.backend.repositories

import app.suhasdissa.memerize.backend.serializables.Children
import app.suhasdissa.memerize.backend.RedditApi

interface RedditRepository {
    suspend fun getData(subreddit: String, time: String): ArrayList<Children>
}

class DefaultRedditRepository : RedditRepository {
    override suspend fun getData(subreddit: String, time: String): ArrayList<Children> {
        return RedditApi.retrofitService.getRedditData(subreddit, time).data.children
    }
}