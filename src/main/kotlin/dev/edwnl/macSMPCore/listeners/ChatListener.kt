package dev.edwnl.macSMPCore.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ChatListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Set join message to our custom format
        event.joinMessage(Component.text()
            .append(Component.text("${event.player.name} joined!", NamedTextColor.GREEN))
            .build()
        )
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Set quit message to our custom format
        event.quitMessage(Component.text()
            .append(Component.text("${event.player.name} left.", NamedTextColor.RED))
            .build()
        )
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerChat(event: AsyncChatEvent) {
        val formattedMessage = Component.text()
            .append(Component.text(event.player.name, NamedTextColor.GREEN))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(event.message())  // This preserves any formatting in the original message
            .build()

        event.renderer { _, _, message, _ ->
            formattedMessage
        }
    }
}