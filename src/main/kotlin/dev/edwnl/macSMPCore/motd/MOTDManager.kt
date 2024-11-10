package dev.edwnl.macSMPCore.motd

import dev.edwnl.macSMPCore.MacSMPCore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import java.text.SimpleDateFormat
import java.util.*

class MOTDManager(private val plugin: MacSMPCore) : Listener {
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a")

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        dateFormatter.timeZone = TimeZone.getTimeZone("Australia/Melbourne")
    }

    @EventHandler
    fun onServerPing(event: ServerListPingEvent) {
        val currentTime = dateFormatter.format(Date())

        // First line: MAC PROJECTS centered with hosting info
        val line1 = Component.text()
            .append(Component.text(centerText("MAC SMP [1.21.3]"))
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD))
            .build()

        // Second line: Current time and hosting information
        val line2 = Component.text()
            .append(Component.text(centerText("Hosted by MAC's Projects Team!"))
                .color(NamedTextColor.GRAY))
            .build()

        // Combine both lines
        event.motd(Component.text()
            .append(line1)
            .append(Component.newline())
            .append(line2).build())
    }

    companion object {
        fun centerText(text: String): String {
            val maxWidth = 50 // Maximum width of MOTD
            val spacesBefore = (maxWidth - text.length) / 2
            return " ".repeat(spacesBefore) + text
        }
    }
}