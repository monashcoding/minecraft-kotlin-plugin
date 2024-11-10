package dev.edwnl.macSMPCore.clearlag

import dev.edwnl.macSMPCore.MacSMPCore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class ClearlagSystem(private val plugin: MacSMPCore) {
    private var task: BukkitTask? = null
    private var warningTask: BukkitTask? = null

    // Warning times in seconds before clear
    private val warningTimes = listOf(60, 30, 10, 5, 4, 3, 2, 1)

    // Time between clears in minutes
    private val clearInterval = 15L

    init {
        startClearlagTask()
    }

    private fun startClearlagTask() {
        task = object : BukkitRunnable() {
            override fun run() {
                startWarningSequence()
            }
        }.runTaskTimer(plugin, 0L, (20L * 60L * clearInterval))
    }

    private fun startWarningSequence() {
        var currentIndex = 0
        val startTime = System.currentTimeMillis()

        warningTask = object : BukkitRunnable() {
            override fun run() {
                val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                val secondsLeft = 60 - elapsedSeconds

                if (currentIndex < warningTimes.size && secondsLeft <= warningTimes[currentIndex]) {
                    broadcastWarning(warningTimes[currentIndex])
                    currentIndex++
                }

                if (secondsLeft <= 0) {
                    val amountRemoved = clearItems()
                    broadcastClear(amountRemoved)
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 5L) // Check every 1/4 second for more precise timing
    }

    private fun broadcastWarning(seconds: Int) {
        val timeText = if (seconds >= 60) {
            "${seconds / 60} minute${if (seconds >= 120) "s" else ""}"
        } else {
            "$seconds second${if (seconds != 1) "s" else ""}"
        }

        val message = Component.text()
            .append(Component.text("Dropped items will be cleared in ", NamedTextColor.GRAY))
            .append(Component.text("${timeText}.", NamedTextColor.YELLOW))
            .build()

        plugin.server.broadcast(message)

        // Play warning sound
        if (seconds <= 30) {
            plugin.server.onlinePlayers.forEach { player ->
                player.playSound(player.location, Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 1f, 1f)
            }
        }
    }

    private fun clearItems(): Int {
        var count = 0

        plugin.server.worlds.forEach { world ->
            world.entities
                .filter { it.type == EntityType.ITEM }
                .forEach { entity ->
                    entity.remove()
                    count++
                }
        }

        return count
    }

    private fun broadcastClear(amountRemoved: Int) {
        val message = Component.text()
            .append(Component.text("$amountRemoved items have been cleared!", NamedTextColor.GREEN))
            .build()

        plugin.server.broadcast(message)
    }

    fun shutdown() {
        task?.cancel()
        warningTask?.cancel()
    }
}