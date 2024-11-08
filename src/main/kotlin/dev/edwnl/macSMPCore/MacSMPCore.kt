package dev.edwnl.macSMPCore

import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.listeners.ChatListener
import dev.edwnl.macSMPCore.listeners.PlayerLoginListener
import dev.edwnl.macSMPCore.listeners.StatsListener
import dev.edwnl.macSMPCore.scoreboard.ScoreboardManager
import dev.edwnl.macSMPCore.stats.StatsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.plugin.java.JavaPlugin
import dev.edwnl.macSMPCore.utils.Bukkit

class MacSMPCore : JavaPlugin() {
    companion object {
        lateinit var instance: MacSMPCore
            private set
    }

    private lateinit var mongoDB: MongoDB
    lateinit var scoreboardManager: ScoreboardManager
    private lateinit var statsManager: StatsManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onEnable() {
        instance = this

        // Initialize configuration
        saveDefaultConfig()

        // Initialize MongoDB connection
        mongoDB = MongoDB(config.getString("mongodb.uri") ?: "")
        scoreboardManager = ScoreboardManager(this)
        statsManager = StatsManager(mongoDB, this, scoreboardManager)

        // Register listeners
        server.pluginManager.registerEvents(PlayerLoginListener(mongoDB), this)
        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(StatsListener(statsManager), this)

        // Schedule periodic stats update using coroutines
        server.scheduler.runTaskTimer(this, Runnable {
            coroutineScope.launch {
                try {
                    // Step 1: Save all stats
                    val saveSuccess = statsManager.saveAllStats()

                    if (saveSuccess) {
                        // Step 2: Update rankings
                        mongoDB.updateAllRankings()

                        // Step 3: Update scoreboards on main thread
                        withContext(Dispatchers.Bukkit) {
                            server.onlinePlayers.forEach { player ->
                                statsManager.statsCache[player.uniqueId]?.let { stats ->
                                    scoreboardManager.createScoreboard(player, stats)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.severe("Failed to update stats and rankings: ${e.message}")
                }
            }
        }, 1200L, 1200L)

        logger.info("MAC SMP Core has been enabled!")
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { player ->
            scoreboardManager.cleanup(player)
        }

        if (::mongoDB.isInitialized) {
            mongoDB.close()
        }
        logger.info("MAC SMP Core has been disabled!")
    }
}