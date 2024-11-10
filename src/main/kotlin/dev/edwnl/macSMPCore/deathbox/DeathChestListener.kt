package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.MacSMPCore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType


class DeathChestListener(private val plugin: MacSMPCore) : Listener {

    companion object {
        private const val DEATH_CHEST_META = "death_chest"
    }

    private val xpKey = NamespacedKey(plugin, "stored_xp")

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // Don't create chest if no items or XP to store
        if (event.drops.isEmpty() && event.droppedExp == 0) return

        val location = findValidChestLocation(event.entity.location)

        // Create the death chest
        location.block.type = Material.CHEST
        val chest = location.block.state as Chest
        chest.setMetadata(DEATH_CHEST_META, FixedMetadataValue(plugin, true))

        // Store items
        val inventory = chest.inventory
        event.drops.forEach { item ->
            inventory.addItem(item)
        }

        if (event.droppedExp > 0) {
            val xpBottle = ItemStack(Material.GLASS_BOTTLE)
            val meta: ItemMeta? = xpBottle.itemMeta
            if (meta != null) {
                meta.displayName(Component.text("Stored Experience", NamedTextColor.GREEN))
                val data: PersistentDataContainer = meta.persistentDataContainer
                data.set(xpKey, PersistentDataType.INTEGER, event.droppedExp)
                xpBottle.itemMeta = meta
                inventory.addItem(xpBottle)
            }
        }
        // Clear original drops
        event.drops.clear()
        event.droppedExp = 0

        // Notify the player
        event.entity.sendMessage("§aYour items have been stored in a death chest at ${formatLocation(location)}. This chest does not expire, but can be opened by anyone.")
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.type != InventoryType.CHEST) return

        val chest = event.inventory.location?.block?.state as? Chest ?: return
        if (!chest.hasMetadata(DEATH_CHEST_META)) return

        // Remove chest if empty
        if (event.inventory.isEmpty) {
            chest.block.type = Material.AIR
            event.player.sendMessage("§aAll items have been collected.")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type != Material.CHEST) return

        val chest = block.state as? Chest ?: return
        if (!chest.hasMetadata(DEATH_CHEST_META)) return

        // Prevent breaking if not empty
        if (!chest.inventory.isEmpty) {
            event.isCancelled = true
            event.player.sendMessage("§cYou cannot break a death chest that still contains items!")
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            val item = event.item ?: return
            if (item.type == Material.GLASS_BOTTLE) {
                val meta = item.itemMeta ?: return
                val data = meta.persistentDataContainer

                if (data.has(xpKey, PersistentDataType.INTEGER)) {
                    val storedXp = data.get(xpKey, PersistentDataType.INTEGER) ?: return
                    val player: Player = event.player
                    player.giveExp(storedXp)
                    event.player.sendMessage("§aYou have received $storedXp XP.")
                    item.amount -= 1
                    event.isCancelled = true
                }
            }
        }
    }

    private fun findValidChestLocation(location: Location): Location {
        val original = location.clone().block.location

        // Check original location first
        if (isValidChestLocation(original)) return original

        // Check adjacent blocks in a spiral pattern
        val directions = listOf(
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
        )

        for (direction in directions) {
            val newLoc = original.block.getRelative(direction).location
            if (isValidChestLocation(newLoc)) return newLoc
        }

        // If no valid location found, force the original location
        // This will replace whatever block was there
        return original
    }

    private fun isValidChestLocation(location: Location): Boolean {
        val block = location.block
        return block.type == Material.AIR ||
                block.type == Material.CAVE_AIR
    }

    private fun formatLocation(location: Location): String {
        return "§fx: ${location.blockX}, y: ${location.blockY}, z: ${location.blockZ}"
    }
}