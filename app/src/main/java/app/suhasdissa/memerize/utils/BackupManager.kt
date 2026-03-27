package app.test2a.memerize.utils

import android.content.Context
import app.test2a.memerize.backend.database.MemeDatabase
import app.test2a.memerize.backend.database.entity.LemmyCommunity
import app.test2a.memerize.backend.database.entity.RedditCommunity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object BackupManager {
    private val json = Json { prettyPrint = true }

    @Serializable
    private data class BackupPayload(
        val subreddits: List<RedditCommunity> = emptyList(),
        val lemmy_instances: List<LemmyCommunity> = emptyList()
    )

    fun exportBackup(context: Context): String {
        val db = MemeDatabase.getDatabase(context)
        val subreddits = db.subredditDao().getAll()
        val communities = db.communityDao().getAll()
        val payload = BackupPayload(
            subreddits = subreddits,
            lemmy_instances = communities
        )
        return json.encodeToString(payload)
    }

    fun importBackup(context: Context, data: String): Boolean {
        return try {
            val payload = json.decodeFromString<BackupPayload>(data)
            val db = MemeDatabase.getDatabase(context)
            if (payload.subreddits.isNotEmpty()) db.subredditDao().insertAll(payload.subreddits)
            if (payload.lemmy_instances.isNotEmpty()) db.communityDao().insertAll(payload.lemmy_instances)
            true
        } catch (e: Exception) {
            InAppLogger.log("Backup import failed: ${e.message}")
            false
        }
    }
}
