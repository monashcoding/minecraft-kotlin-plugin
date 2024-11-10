package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.stats.StatsManager
import dev.edwnl.macSMPCore.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class StatsListener(plugin: JavaPlugin) : Listener {
    private val statsManager = StatsManager.getInstance()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Update playtime every second instead of every minute
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            plugin.server.onlinePlayers.forEach { player ->
                statsManager.updatePlaytime(player)
            }
        }, 20L, 20L) // 20 ticks = 1 second
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        scope.launch {
            statsManager.loadPlayerStats(event.player)
        }
    }

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        // Make sure it's a real advancement with a display
        // (some advancements are internal and don't have displays)
        val advancement = event.advancement
        if (advancement.display == null) return;

        val player = event.player

        val advancementCount = player.server.advancementIterator().asSequence()
            .filter { adv ->
                player.getAdvancementProgress(adv).isDone && adv.display != null
            }
            .count()

        statsManager.updateAchievements(player, advancementCount)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        statsManager.handlePlayerQuit(event.player)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.to.toBlockLocation() != event.from.toBlockLocation()) {
            val distance = event.from.distance(event.to)
            statsManager.incrementDistance(event.player, distance)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        statsManager.incrementBlocks(event.player)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        statsManager.incrementBlocks(event.player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        statsManager.incrementDeaths(event.player)
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        event.entity.killer?.let { killer ->
            statsManager.incrementKills(killer)
        }
    }
}