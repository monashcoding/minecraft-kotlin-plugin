package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.database.MongoDB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.inventory.ItemStack

class PlayerLoginListener(private val mongoDB: MongoDB) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (!mongoDB.isPlayerWhitelisted(event.uniqueId)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                Component.text("You are not whitelisted. Use /whitelist add ${event.playerProfile.name} on the MAC discord!")
                    .color(NamedTextColor.RED)
            )
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player

        // Check if this is the player's first time joining
        if (!player.hasPlayedBefore()) {
            // Create starter food package
            val starterFood = ItemStack(Material.COOKED_BEEF, 16)

            // Add items to player's inventory
            player.inventory.addItem(starterFood)
        }
    }
}