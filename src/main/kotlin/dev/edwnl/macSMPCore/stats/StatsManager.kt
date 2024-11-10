package dev.edwnl.macSMPCore.stats

import dev.edwnl.macSMPCore.managers.AFKManager
import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.listeners.ChatListener
import dev.edwnl.macSMPCore.listeners.StatsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StatsManager private constructor() {
    private lateinit var plugin: JavaPlugin
    private lateinit var database: MongoDB
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cache of player stats
    private val playerStats = ConcurrentHashMap<UUID, PlayerStats>()
    private val distanceBuffer = ConcurrentHashMap<UUID, Double>()

    companion object {
        @Volatile
        private var instance: StatsManager? = null
        private const val SAVE_INTERVAL_TICKS = 6000L // 5 minutes

        fun getInstance(): StatsManager {
            return instance ?: synchronized(this) {
                instance ?: StatsManager().also { instance = it }
            }
        }
    }

    fun initialize(plugin: JavaPlugin, database: MongoDB) {
        if (this::plugin.isInitialized) return

        plugin.server.pluginManager.registerEvents(StatsListener(plugin), plugin)

        this.plugin = plugin
        this.database = database

        startPeriodicSave()
    }

    private fun startPeriodicSave() {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            saveAllPlayersAndUpdateRankings()
        }, SAVE_INTERVAL_TICKS, SAVE_INTERVAL_TICKS)
    }

    private fun saveAllPlayersAndUpdateRankings() {
        scope.launch {
            // Save all online players' stats
            playerStats.values.forEach { stats ->
                database.savePlayerStats(stats)
            }

            // Update rankings for all players
            database.updateAllRankings()

            // Update only rankings for online players
            plugin.server.onlinePlayers.forEach { player ->
                updatePlayerRankings(player)
            }
        }
    }

    private suspend fun updatePlayerRankings(player: Player) {
        val currentStats = playerStats[player.uniqueId] ?: return
        val databaseStats = database.loadPlayerStats(player.uniqueId.toString()) ?: return

        // Only update the rankings, preserve the current stats
        currentStats.rankings = databaseStats.rankings
    }

    suspend fun loadPlayerStats(player: Player) {
        val uuid = player.uniqueId

        // If player already exists in cache, only update rankings
        if (playerStats.containsKey(uuid)) {
            updatePlayerRankings(player)
            return
        }

        // Otherwise, load full stats for new player
        val stats = database.loadPlayerStats(uuid.toString())
            ?: PlayerStats(
                uuid = uuid.toString(),
                username = player.name
            )

        playerStats[uuid] = stats
    }

    fun getStats(player: Player): PlayerStats? {
        return playerStats[player.uniqueId]
    }

    // Now tracking time in seconds instead of minutes
    fun updatePlaytime(player: Player) {
        if (AFKManager.getInstance().isAFK(player)) return

        getStats(player)?.let { stats ->
            stats.playtime += 1 // Add one second
            stats.lastUpdated = Date()
        }
    }

    fun incrementDistance(player: Player, blocks: Double) {
        if (AFKManager.getInstance().isAFK(player)) return

        val uuid = player.uniqueId
        val currentBuffer = distanceBuffer.getOrDefault(uuid, 0.0) + blocks

        // If we've accumulated at least one block worth of distance
        if (currentBuffer >= 1.0) {
            val fullBlocks = currentBuffer.toLong()
            getStats(player)?.let { stats ->
                stats.distance += fullBlocks
                stats.lastUpdated = Date()
            }
            // Store remaining fraction
            distanceBuffer[uuid] = currentBuffer - fullBlocks
        } else {
            // Just update the buffer
            distanceBuffer[uuid] = currentBuffer
        }
    }

    fun incrementDeaths(player: Player) {
        getStats(player)?.let { stats ->
            stats.deaths++
            stats.lastUpdated = Date()
        }
    }

    fun incrementKills(player: Player) {
        getStats(player)?.let { stats ->
            stats.kills++
            stats.lastUpdated = Date()
        }
    }

    fun incrementBlocks(player: Player) {
        getStats(player)?.let { stats ->
            stats.blocks++
            stats.lastUpdated = Date()
        }
    }

    fun updateAchievements(player: Player, count: Int) {
        getStats(player)?.let { stats ->
            stats.achievements = count
            stats.lastUpdated = Date()
        }
    }

    fun handlePlayerQuit(player: Player) {
        scope.launch {
            getStats(player)?.let { stats ->
                database.savePlayerStats(stats)
            }
            playerStats.remove(player.uniqueId)
        }
    }
}