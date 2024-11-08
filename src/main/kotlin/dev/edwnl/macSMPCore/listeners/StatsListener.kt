package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.stats.StatsManager
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

class StatsListener(private val statsManager: StatsManager) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        statsManager.loadPlayerStats(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        statsManager.unloadPlayerStats(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        if (!player.hasMetadata("NPC")) {
            statsManager.incrementDeaths(player)

            // Handle killer stats
            event.entity.killer?.let { killer ->
                if (!killer.hasMetadata("NPC")) {
                    statsManager.incrementKills(killer)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityKill(event: EntityDeathEvent) {
        val killer = event.entity.killer
        if (killer is Player && !killer.hasMetadata("NPC")) {
            statsManager.incrementKills(killer)
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        if (!event.player.hasMetadata("NPC")) {
            // Count all awarded criteria across all advancements
            val totalCriteria = event.player.server.advancementIterator().asSequence()
                .sumOf { advancement ->
                    event.player.getAdvancementProgress(advancement).awardedCriteria.size
                }

            statsManager.updateAchievements(event.player, totalCriteria)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.player.hasMetadata("NPC")) {
            val from = event.from
            val to = event.to ?: return

            // Only process if there's actual movement (not just head rotation)
            if (from.x != to.x || from.y != to.y || from.z != to.z) {
                // Calculate 3D distance (you could also use horizontal distance if preferred)
                val distance = from.distance(to)

                // Ignore very small movements (might be due to server corrections)
                if (distance > 0.01) {  // Threshold to filter out minimal movements
                    statsManager.incrementDistance(event.player, distance)
                    statsManager.recordActivity(event.player)
                }
            }
        }
    }

    // Record activity for various player actions
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        statsManager.recordActivity(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        statsManager.recordActivity(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        statsManager.recordActivity(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked is Player) {
            statsManager.recordActivity(event.whoClicked as Player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        statsManager.recordActivity(event.player)
        statsManager.incrementBlocks(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        statsManager.recordActivity(event.player)
        statsManager.incrementBlocks(event.player)
    }
}