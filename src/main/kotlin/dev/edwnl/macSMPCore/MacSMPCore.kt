package dev.edwnl.macSMPCore

import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.listeners.ChatListener
import dev.edwnl.macSMPCore.listeners.PlayerLoginListener
import dev.edwnl.macSMPCore.managers.AFKManager
import dev.edwnl.macSMPCore.scoreboard.ScoreboardManager
import dev.edwnl.macSMPCore.scoreboard.TabListManager
import dev.edwnl.macSMPCore.stats.StatsManager
import org.bukkit.plugin.java.JavaPlugin

class MacSMPCore : JavaPlugin() {
    companion object {
        lateinit var instance: MacSMPCore
            private set
    }

    private lateinit var mongoDB: MongoDB

    override fun onEnable() {
        instance = this

        // Initialize configuration
        saveDefaultConfig()

        // Initialize MongoDB connection
        mongoDB = MongoDB(config.getString("mongodb.uri") ?: "")

        // Register listeners
        server.pluginManager.registerEvents(PlayerLoginListener(mongoDB), this)
        server.pluginManager.registerEvents(ChatListener(), this)

        AFKManager.getInstance().initialize(this)
        ScoreboardManager.getInstance().initialize(this)
        StatsManager.getInstance().initialize(this, mongoDB)

        TabListManager(this);

        logger.info("MAC SMP Core has been enabled!")
    }

    override fun onDisable() {
        if (::mongoDB.isInitialized) {
            mongoDB.close()
        }

        server.onlinePlayers.forEach { player ->
            AFKManager.getInstance().cleanup(player);
        }

        ScoreboardManager.getInstance().cleanup()
        logger.info("MAC SMP Core has been disabled!")
    }
}