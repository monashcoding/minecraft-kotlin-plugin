package dev.edwnl.macSMPCore.stats

import dev.edwnl.macSMPCore.MacSMPCore
import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.scoreboard.ScoreboardManager
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import dev.edwnl.macSMPCore.utils.Bukkit
import java.lang.Runnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StatsManager(
    private val mongoDB: MongoDB,
    private val plugin: MacSMPCore,
    private val scoreboardManager: ScoreboardManager
) {
    val statsCache = ConcurrentHashMap<UUID, PlayerStats>()
    private val lastActivityTime = ConcurrentHashMap<UUID, Long>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    // Track partial distances until they accumulate to full blocks
    private val distanceAccumulator = ConcurrentHashMap<UUID, Double>()

    init {
        // Schedule playtime updates every minute
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            updatePlaytimeForActivePlayers()
        }, 1200L, 1200L) // 1200 ticks = 1 minute
    }

    fun recordActivity(player: Player) {
        lastActivityTime[player.uniqueId] = System.currentTimeMillis()
    }

    fun incrementDistance(player: Player, distance: Double) {
        if (!player.hasMetadata("NPC")) {
            statsCache[player.uniqueId]?.let { stats ->
                // Add to the accumulator
                val currentAccumulated = distanceAccumulator.getOrDefault(player.uniqueId, 0.0)
                val newAccumulated = currentAccumulated + distance

                // If we've accumulated at least one block, update the stats
                if (newAccumulated >= 1.0) {
                    val blocksToAdd = newAccumulated.toLong()
                    stats.distance += blocksToAdd
                    // Store the remainder
                    distanceAccumulator[player.uniqueId] = newAccumulated - blocksToAdd
                    updateStatsAndScoreboard(player)
                } else {
                    // Just store the accumulated distance
                    distanceAccumulator[player.uniqueId] = newAccumulated
                }
            }
        }
    }

    private fun isPlayerAFK(player: Player): Boolean {
        val lastActivity = lastActivityTime[player.uniqueId] ?: return true
        val afkThreshold = 5 * 60 * 1000 // 5 minutes in milliseconds
        return System.currentTimeMillis() - lastActivity > afkThreshold
    }

    private fun updatePlaytimeForActivePlayers() {
        plugin.server.onlinePlayers.forEach { player ->
            if (!player.hasMetadata("NPC") && !isPlayerAFK(player)) {
                statsCache[player.uniqueId]?.let { stats ->
                    stats.playtime += 1 // Increment by 1 minute
                    updateStatsAndScoreboard(player)
                }
            }
        }
    }

    fun loadPlayerStats(player: Player) {
        coroutineScope.launch {
            val stats = mongoDB.loadPlayerStats(player.uniqueId.toString()) ?: PlayerStats(
                uuid = player.uniqueId.toString(),
                username = player.name
            )
            statsCache[player.uniqueId] = stats

            // Update scoreboard on the main thread after stats are loaded
            withContext(Dispatchers.Bukkit) {
                scoreboardManager.createScoreboard(player, stats)
            }
        }
    }

    fun unloadPlayerStats(player: Player) {
        statsCache[player.uniqueId]?.let { stats ->
            // Save stats synchronously when player quits
            runBlocking {
                try {
                    mongoDB.savePlayerStats(stats)
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to save stats for ${player.name}: ${e.message}")
                }
            }
        }
        statsCache.remove(player.uniqueId)
        lastActivityTime.remove(player.uniqueId)
        scoreboardManager.cleanup(player)
    }

    private fun updateStatsAndScoreboard(player: Player) {
        statsCache[player.uniqueId]?.let { stats ->
            scoreboardManager.scheduleUpdate(player, stats)
        }
    }

    // Statistics update methods
    fun incrementKills(player: Player) {
        statsCache[player.uniqueId]?.let {
            it.kills++
            updateStatsAndScoreboard(player)
        }
    }

    fun incrementDeaths(player: Player) {
        statsCache[player.uniqueId]?.let {
            it.deaths++
            updateStatsAndScoreboard(player)
        }
    }

    fun incrementBlocks(player: Player) {
        statsCache[player.uniqueId]?.let {
            it.blocks++
            updateStatsAndScoreboard(player)
        }
    }

    fun updateAchievements(player: Player, count: Int) {
        statsCache[player.uniqueId]?.let {
            it.achievements = count
            updateStatsAndScoreboard(player)
        }
    }

    suspend fun saveAllStats(): Boolean = withContext(Dispatchers.IO) {
        try {
            val statsSnapshot = HashMap(statsCache)
            statsSnapshot.values.forEach { stats ->
                mongoDB.savePlayerStats(stats)
            }
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save stats: ${e.message}")
            false
        }
    }
}