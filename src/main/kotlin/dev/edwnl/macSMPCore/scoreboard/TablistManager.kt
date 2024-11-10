package dev.edwnl.macSMPCore.scoreboard

import dev.edwnl.macSMPCore.MacSMPCore
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import kotlin.math.round

class TabListManager(private val plugin: MacSMPCore) {
    init {
        startUpdateTask()
    }

    private fun startUpdateTask() {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            updateTabList()
        }, 20L, 20L) // 20 ticks = 1 second
    }

    private fun updateTabList() {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendPlayerListHeaderAndFooter(buildHeader(player), buildFooter(player))
        }
    }

    private fun buildHeader(player: Player): Component {
        val location = player.location;

        return Component.text()
            .append(
                Component.text("MAC PROJECTS", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.BOLD, true)
            )
            .append(Component.newline())
            .append(
                Component.text(
                    "Location: X ${round(location.x)} Y ${round(location.y)} Z ${round(location.z)}",
                    NamedTextColor.GRAY
                )
            )
            .append(Component.newline())
            .build()
    }

    private fun buildFooter(player: Player): Component {
        val tps = PlaceholderAPI.setPlaceholders(player, "%spark_tps_5s% %spark_tps_1m% %spark_tps_15m%")
        val mspt = PlaceholderAPI.setPlaceholders(player, "%spark_tickduration_1m%")

        return Component.text()
            .append(Component.newline())
            .append(
                Component.text("Online Players: ", NamedTextColor.WHITE)
                    .append(Component.text(Bukkit.getOnlinePlayers().size.toString(), NamedTextColor.GREEN))
            )
            .append(Component.newline())
            .append(
                Component.text("TPS (5s, 1m, 15m): ", NamedTextColor.WHITE)
                    .append(Component.text(tps, NamedTextColor.GREEN))
            )
            .append(Component.newline())
            .append(
                Component.text("MSPT (min/med/95%/max): ", NamedTextColor.WHITE)
                    .append(Component.text(mspt, NamedTextColor.GREEN))
            )
            .append(Component.newline())
            .append(
                Component.text("monashcoding.com", NamedTextColor.YELLOW)
            )
            .build()
    }
}