package dev.edwnl.macSMPCore

import org.bukkit.plugin.java.JavaPlugin

class MacSMPCore : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        // Initialize configuration
        saveDefaultConfig()

        // Initialize MongoDB connection
        mongoDB = MongoDB(config.getString("mongodb.uri") ?: "")

        // Register listeners
        server.pluginManager.registerEvents(PlayerLoginListener(mongoDB), this)

        logger.info("MAC SMP Core has been enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic

        if (::mongoDB.isInitialized) {
            mongoDB.close()
        }
        logger.info("MAC SMP Core has been disabled!")
    }
}
