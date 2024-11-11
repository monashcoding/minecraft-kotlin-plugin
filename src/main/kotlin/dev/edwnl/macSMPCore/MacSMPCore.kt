package dev.edwnl.macSMPCore

import dev.edwnl.macSMPCore.afk.AFKManager
import dev.edwnl.macSMPCore.clearlag.ClearlagSystem
import dev.edwnl.macSMPCore.database.MongoDB
import dev.edwnl.macSMPCore.deathbox.ClaimChestCommand
import dev.edwnl.macSMPCore.listeners.ChatListener
import dev.edwnl.macSMPCore.listeners.DeathChestListener
import dev.edwnl.macSMPCore.listeners.EndPortalListener
import dev.edwnl.macSMPCore.listeners.PlayerLoginListener
import dev.edwnl.macSMPCore.motd.MOTDManager
import dev.edwnl.macSMPCore.scoreboard.ScoreboardCommand
import dev.edwnl.macSMPCore.scoreboard.ScoreboardManager
import dev.edwnl.macSMPCore.sleep.SleepManager
import dev.edwnl.macSMPCore.stats.StatsManager
import dev.edwnl.macSMPCore.tablist.TabListManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class MacSMPCore : JavaPlugin() {
    companion object {
        lateinit var instance: MacSMPCore
            private set
    }

    private lateinit var mongoDB: MongoDB
//    private lateinit var clearlagSystem: ClearlagSystem

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

        // MOTD Module
        MOTDManager(this);

//        clearlagSystem = ClearlagSystem(this);

        server.pluginManager.registerEvents(EndPortalListener(this), this)

        getCommand("claimchest")?.setExecutor(ClaimChestCommand(this))
        getCommand("scoreboard")?.setExecutor(ScoreboardCommand())

        logger.info("MAC SMP Core has been enabled!")
    }

    override fun onDisable() {
        if (::mongoDB.isInitialized) {
            mongoDB.close()
        }

        AFKManager.getInstance().cleanup();
        ScoreboardManager.getInstance().cleanup()
//        clearlagSystem.shutdown();

        logger.info("MAC SMP Core has been disabled!")
    }
}