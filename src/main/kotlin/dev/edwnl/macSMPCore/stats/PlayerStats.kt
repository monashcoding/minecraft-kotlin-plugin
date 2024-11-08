package dev.edwnl.macSMPCore.stats

import org.bson.Document
import java.util.*

data class Rankings(
    var playtimeRank: Int = 0,
    var achievementsRank: Int = 0,
    var deathsRank: Int = 0,
    var killsRank: Int = 0,
    var blocksRank: Int = 0,
    var distanceRank: Int = 0,
    var lastCalculated: Date = Date()
) {
    fun toDocument(): Document = Document().apply {
        append("playtimeRank", playtimeRank)
        append("achievementsRank", achievementsRank)
        append("deathsRank", deathsRank)
        append("killsRank", killsRank)
        append("blocksRank", blocksRank)
        append("distanceRank", distanceRank)
        append("lastCalculated", lastCalculated)
    }

    companion object {
        fun fromDocument(doc: Document): Rankings = Rankings(
            playtimeRank = doc.getInteger("playtimeRank", 0),
            achievementsRank = doc.getInteger("achievementsRank", 0),
            deathsRank = doc.getInteger("deathsRank", 0),
            killsRank = doc.getInteger("killsRank", 0),
            blocksRank = doc.getInteger("blocksRank", 0),
            distanceRank = doc.getInteger("distanceRank", 0),
            lastCalculated = doc.getDate("lastCalculated") ?: Date()
        )
    }
}

data class PlayerStats(
    val uuid: String,
    val username: String,
    var playtime: Long = 0,      // stored in minutes
    var achievements: Int = 0,
    var deaths: Int = 0,
    var kills: Int = 0,
    var blocks: Long = 0,
    var distance: Long = 0,
    var rankings: Rankings = Rankings(),
    var lastUpdated: Date = Date()
) {
    fun toDocument(): Document = Document().apply {
        append("uuid", uuid)
        append("username", username)
        append("playtime", playtime)
        append("achievements", achievements)
        append("deaths", deaths)
        append("kills", kills)
        append("blocks", blocks)
        append("distance", distance)
        append("rankings", rankings.toDocument())
        append("lastUpdated", lastUpdated)
    }

    companion object {
        fun fromDocument(doc: Document): PlayerStats = PlayerStats(
            uuid = doc.getString("uuid"),
            username = doc.getString("username"),
            playtime = doc.getLong("playtime"),
            achievements = doc.getInteger("achievements"),
            deaths = doc.getInteger("deaths"),
            kills = doc.getInteger("kills"),
            blocks = doc.getLong("blocks"),
            distance = doc.getLong("distance"),
            rankings = Rankings.fromDocument(doc.get("rankings", Document())),
            lastUpdated = doc.getDate("lastUpdated") ?: Date()
        )
    }
}