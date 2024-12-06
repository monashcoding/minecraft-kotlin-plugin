package dev.edwnl.macSMPCore.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.result.UpdateResult
import dev.edwnl.macSMPCore.stats.PlayerStats
import org.bson.Document
import java.util.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MongoDB(uri: String) {
    private val client: MongoClient = MongoClients.create(uri)
    private val database = client.getDatabase("minecraft-discordjs-bot")
    private val whitelistCollection: MongoCollection<Document> =
        database.getCollection("WhitelistedPlayer")
    private val statsCollection = database.getCollection("playerStats")

    init {
        // Create indexes for efficient sorting
        statsCollection.createIndex(Document("playtime", -1))
        statsCollection.createIndex(Document("achievements", -1))
        statsCollection.createIndex(Document("kills", -1))
        statsCollection.createIndex(Document("deaths", -1))
        statsCollection.createIndex(Document("blocks", -1))
        statsCollection.createIndex(Document("distance", -1))
    }

    suspend fun loadPlayerStats(uuid: String): PlayerStats? = withContext(Dispatchers.IO) {
        statsCollection
            .find(Filters.eq("uuid", uuid))
            .first()
            ?.let { PlayerStats.fromDocument(it) }
    }

    suspend fun savePlayerStats(stats: PlayerStats): UpdateResult = withContext(Dispatchers.IO) {
        statsCollection.updateOne(
            Filters.eq("uuid", stats.uuid),
            Updates.combine(
                Updates.set("username", stats.username),
                Updates.set("playtime", stats.playtime),
                Updates.set("achievements", stats.achievements),
                Updates.set("deaths", stats.deaths),
                Updates.set("kills", stats.kills),
                Updates.set("blocks", stats.blocks),
                Updates.set("distance", stats.distance),
                Updates.set("lastUpdated", Date())
            ),
            UpdateOptions().upsert(true)
        )
    }

    suspend fun updateAllRankings(): UpdateResult = withContext(Dispatchers.IO) {
        val statFields = listOf(
            "playtime",
            "achievements",
            "kills",
            "deaths",
            "blocks",
            "distance"
        )

        statFields.forEach { statField ->
            val rankedPlayers = statsCollection
                .find()
                .sort(Sorts.descending(statField))
                .toList()

            rankedPlayers.forEachIndexed { index, player ->
                val rank = index + 1
                statsCollection.updateOne(
                    Filters.eq("_id", player.getObjectId("_id")),
                    Updates.set("rankings.${statField}Rank", rank)
                )
            }
        }

        // Update lastCalculated timestamp for all players
        statsCollection.updateMany(
            Filters.exists("_id"),
            Updates.set("rankings.lastCalculated", Date())
        )
    }

    /**
     * Checks if a player's UUID is in the whitelist and is active
     * @param uuid The UUID to check (without dashes)
     * @return true if the player is whitelisted and active, false otherwise
     */
    fun isPlayerWhitelisted(uuid: UUID): Boolean {
        val uuidWithoutDashes = uuid.toString().replace("-", "")
        val query = Document().apply {
            append("minecraftUuid", uuidWithoutDashes)
            append("isActive", true)
        }
        return whitelistCollection.find(query).first() != null
    }



    /**
     * Closes the MongoDB connection
     */
    fun close() {
        client.close()
    }
}