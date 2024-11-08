package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.database.MongoDB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class PlayerLoginListener(private val mongoDB: MongoDB) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (!mongoDB.isPlayerWhitelisted(event.uniqueId)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                Component.text("You are not whitelisted. Use /whitelist add [username] on the MAC discord!")
                    .color(NamedTextColor.RED)
            )
        }
    }
}