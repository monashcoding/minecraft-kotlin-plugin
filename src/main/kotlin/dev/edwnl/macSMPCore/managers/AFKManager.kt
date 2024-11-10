package dev.edwnl.macSMPCore.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AFKManager private constructor() {
    private val lastActivityTimes = ConcurrentHashMap<UUID, Long>()
    private val afkPlayers = ConcurrentHashMap<UUID, Long>()
    private val afkThresholdMinutes = 5L
    private lateinit var plugin: JavaPlugin

    companion object {
        @Volatile
        private var instance: AFKManager? = null

        fun getInstance(): AFKManager {
            return instance ?: synchronized(this) {
                instance ?: AFKManager().also { instance = it }
            }
        }
    }

    fun initialize(plugin: JavaPlugin) {
        if (this::plugin.isInitialized) return

        this.plugin = plugin
        plugin.server.pluginManager.registerEvents(AFKListener(), plugin)
        startAFKCheckTask()
    }

    private fun startAFKCheckTask() {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            val currentTime = System.currentTimeMillis()

            plugin.server.onlinePlayers.forEach { player ->
                val lastActivity = lastActivityTimes[player.uniqueId] ?: return@forEach
                val timeSinceActivity = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastActivity)

                if (timeSinceActivity >= afkThresholdMinutes && !isAFK(player)) {
                    setAFK(player)
                }
            }
        }, 20L * 60, 20L * 60) // Check every minute
    }

    fun updateActivity(player: Player) {
        val wasAFK = isAFK(player)
        lastActivityTimes[player.uniqueId] = System.currentTimeMillis()

        if (wasAFK) {
            removeAFK(player)
        }
    }

    fun isAFK(player: Player): Boolean {
        return afkPlayers.containsKey(player.uniqueId)
    }

    private fun setAFK(player: Player) {
        afkPlayers[player.uniqueId] = System.currentTimeMillis()

        val message = Component.text()
            .append(Component.text(player.name, NamedTextColor.GRAY))
            .append(Component.text(" is now AFK", NamedTextColor.GRAY))
            .build()

        plugin.server.broadcast(message)
    }

    private fun removeAFK(player: Player) {
        afkPlayers.remove(player.uniqueId)

        val message = Component.text()
            .append(Component.text(player.name, NamedTextColor.GRAY))
            .append(Component.text(" is no longer AFK", NamedTextColor.GRAY))
            .build()

        plugin.server.broadcast(message)
    }

    fun cleanup(player: Player) {
        lastActivityTimes.remove(player.uniqueId)
        afkPlayers.remove(player.uniqueId)
    }

    inner class AFKListener : Listener {
        @EventHandler
        fun onPlayerMove(event: PlayerMoveEvent) {
            if (event.to.toBlockLocation() != event.from.toBlockLocation()) {
                updateActivity(event.player)
            }
        }

        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            updateActivity(event.player)
        }

        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            cleanup(event.player)
        }
    }
}