package dev.edwnl.macSMPCore.sleep

import dev.edwnl.macSMPCore.MacSMPCore
import dev.edwnl.macSMPCore.afk.AFKManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import kotlin.math.min

class SleepManager(private val plugin: MacSMPCore) {
    var isNightMessageSent = false

    fun initialize() {
        plugin.server.pluginManager.registerEvents(SleepListener(this), plugin)
        // Check time every second to detect night
        plugin.server.scheduler.runTaskTimer(plugin, ::checkNightTime, 0L, 20L)
    }

    private fun isNightOrStormy(world: World): Boolean {
        return world.time in 12542..23459 || world.isThundering
    }

    private fun checkNightTime() {
        plugin.server.worlds
            .filter { it.environment == World.Environment.NORMAL }
            .forEach { world ->

                if (isNightOrStormy(world) && !isNightMessageSent) {
                    broadcastSleepMessage(world)
                    isNightMessageSent = true
                } else if (!isNightOrStormy(world)) {
                    isNightMessageSent = false
                }
            }
    }

    private fun broadcastSleepMessage(world: World) {
        world.players.forEach { player ->
            player.sendMessage(Component.text()
                .append(Component.text("☾ ", NamedTextColor.DARK_AQUA))
                .append(Component.text("Night has fallen. Sleep to skip the night! ", NamedTextColor.DARK_AQUA))
                .append(Component.text("(0/${requiredPlayers(world)})", NamedTextColor.YELLOW))
                .build()
            )
        }
    }

    private fun requiredPlayers(world: World): Int {
        return min(1, world.players.count { !AFKManager.getInstance().isAFK(it) });
    }

    fun handleBedEnter(player: Player) {
        val world = player.world
        if (world.environment != World.Environment.NORMAL) return
        if (!isNightOrStormy(world)) return;

        val sleepingPlayers = world.players.count {
            it.isSleeping && !AFKManager.getInstance().isAFK(it)
        } + 1

        world.players.forEach { p ->
            p.sendMessage(Component.text()
                .append(Component.text("☾ ", NamedTextColor.DARK_AQUA))
                .append(Component.text("${player.name} is now sleeping! ", NamedTextColor.DARK_AQUA))
                .append(Component.text("($sleepingPlayers/${requiredPlayers(world)})", NamedTextColor.YELLOW))
                .build()
            )
        }

        // If all active players are sleeping, skip the night
        if (sleepingPlayers == requiredPlayers(world)) {
            skipNight(world)
        }
    }

    fun handleBedLeave(player: Player) {
        val world = player.world
        if (world.environment != World.Environment.NORMAL) return
        if (!isNightOrStormy(world)) return;

        val totalPlayers = world.players.count { !AFKManager.getInstance().isAFK(it) }
        val sleepingPlayers = world.players.count {
            it.isSleeping && !AFKManager.getInstance().isAFK(it)
        } - 1

        world.players.forEach { p ->
            p.sendMessage(Component.text()
                .append(Component.text("☾ ", NamedTextColor.DARK_AQUA))
                .append(Component.text("${player.name} left their bed. ", NamedTextColor.DARK_AQUA))
                .append(Component.text("($sleepingPlayers/$totalPlayers)", NamedTextColor.YELLOW))
                .build()
            )
        }
    }

    private fun skipNight(world: World) {
        world.time = 0
        world.setStorm(false)
        world.isThundering = false

        world.players.forEach { player ->
            player.sendMessage(Component.text()
                .append(Component.text("☀ ", NamedTextColor.YELLOW))
                .append(Component.text("Good morning! ", NamedTextColor.YELLOW))
                .build()
            )
        }
    }
}

class SleepListener(private val sleepManager: SleepManager) : Listener {
    @EventHandler
    fun onPlayerEnterBed(event: PlayerBedEnterEvent) {
        if (event.bedEnterResult == PlayerBedEnterEvent.BedEnterResult.OK) {
            sleepManager.handleBedEnter(event.player)
        }
    }

    @EventHandler
    fun onPlayerLeaveBed(event: PlayerBedLeaveEvent) {
        sleepManager.handleBedLeave(event.player)
    }

    @EventHandler
    fun onThunderChange(event: ThunderChangeEvent) {
        // Reset night message flag when thunder starts
        if (event.toThunderState()) {
            sleepManager.isNightMessageSent = false
        }
    }

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        // Reset night message flag when it starts raining
        if (event.toWeatherState()) {
            sleepManager.isNightMessageSent = false
        }
    }
}