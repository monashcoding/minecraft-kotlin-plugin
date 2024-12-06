package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.MacSMPCore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.BlockPlaceEvent

class EndPortalListener(private val plugin: MacSMPCore) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Prevent placing eyes of ender in frames
        if (event.action == Action.RIGHT_CLICK_BLOCK &&
            event.clickedBlock?.type == Material.END_PORTAL_FRAME &&
            event.item?.type == Material.ENDER_EYE) {
            event.isCancelled = true
            event.player.sendMessage(
                Component.text("The End is currently disabled! " +
                        "It will be enabled at a set time in the near future.")
                    .color(NamedTextColor.RED)
            )
        }
    }
}