/*******************************************************************************
Created By Suhas Dissanayake on 7/29/23, 8:14 PM
Copyright (c) 2023
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.test2a.memerize.backend.database.entity.RedditMeme

@Dao
interface RedditMemeDao {
    @Query("SELECT * FROM reddit_table WHERE subreddit=:subreddit")
    fun getAll(subreddit: String): List<RedditMeme>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(memes: List<RedditMeme>)

    @Delete
    fun delete(meme: RedditMeme)
}
