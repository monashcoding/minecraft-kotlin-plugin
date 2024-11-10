package dev.edwnl.macSMPCore

import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.listeners.ChatListener
import dev.edwnl.macSMPCore.listeners.DeathChestListener
import dev.edwnl.macSMPCore.listeners.PlayerLoginListener
import dev.edwnl.macSMPCore.afk.AFKManager
import dev.edwnl.macSMPCore.scoreboard.ScoreboardManager
import dev.edwnl.macSMPCore.tablist.TabListManager
import dev.edwnl.macSMPCore.sleep.SleepManager
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

        // Whitelist module
        server.pluginManager.registerEvents(PlayerLoginListener(mongoDB), this)

        // Chat format module
        server.pluginManager.registerEvents(ChatListener(), this)

        // Death chest module
        server.pluginManager.registerEvents(DeathChestListener(this), this)

        // AFK detection module
        AFKManager.getInstance().initialize(this)

        // Scoreboard module
        ScoreboardManager.getInstance().initialize(this)

        // Tab list module
        TabListManager(this);

        // Player stats module
        StatsManager.getInstance().initialize(this, mongoDB)

        // Night skip module
        SleepManager(this).initialize();

        getCommand("claimchest")?.setExecutor(ClaimChestCommand(this))
        logger.info("MAC SMP Core has been enabled!")
    }

    override fun onDisable() {
        if (::mongoDB.isInitialized) {
            mongoDB.close()
        }

        AFKManager.getInstance().cleanup();
        ScoreboardManager.getInstance().cleanup()

        logger.info("MAC SMP Core has been disabled!")
    }
}