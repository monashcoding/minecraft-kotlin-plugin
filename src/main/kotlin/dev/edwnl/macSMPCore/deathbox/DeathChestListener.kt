package dev.edwnl.macSMPCore.listeners

import dev.edwnl.macSMPCore.MacSMPCore
import dev.edwnl.macSMPCore.deathbox.DeathChestUtils.Companion.createDeathSkull
import dev.edwnl.macSMPCore.deathbox.DeathChestUtils.Companion.createDoubleChest
import dev.edwnl.macSMPCore.deathbox.DeathChestUtils.Companion.findValidDoubleChestLocation
import dev.edwnl.macSMPCore.utils.Utils.Companion.formatLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
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
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

import org.bukkit.block.data.type.Chest.Type
import org.bukkit.inventory.Inventory
import org.bukkit.block.data.type.Chest as ChestData

class DeathChestListener(private val plugin: MacSMPCore) : Listener {

    companion object {
        const val DEATH_CHEST_META = "death_chest"
    }

    private val xpKey = NamespacedKey(plugin, "stored_xp")

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // Don't create chest if no items or XP to store
        if (event.drops.isEmpty() && event.droppedExp == 0) return

        val location = findValidDoubleChestLocation(event.entity.location)

        // Create the double chest
        val inventory = createDoubleChest(location, event.entity, plugin)

        // Create and add player head with death cause
        val skull = createDeathSkull(event.entity, event.deathMessage())

        // Store items
        inventory.addItem(skull)
        event.drops.forEach { item -> inventory.addItem(item) }

        if (event.droppedExp > 0) {
            val xpBottle = ItemStack(Material.EXPERIENCE_BOTTLE)
            val meta: ItemMeta = xpBottle.itemMeta
            meta.displayName(Component.text("${event.droppedExp} Experience (Right-Click)", NamedTextColor.GREEN))
            val data: PersistentDataContainer = meta.persistentDataContainer
            data.set(xpKey, PersistentDataType.INTEGER, event.droppedExp)
            xpBottle.itemMeta = meta
            inventory.addItem(xpBottle)
        }

        // Clear original drops
        event.drops.clear()
        event.droppedExp = 0

        // Notify the player
        event.entity.sendMessage(Component.text("Your items have been stored in a death chest at ${formatLocation(location)}. ", NamedTextColor.GREEN))
        event.entity.sendMessage(Component.text("If you can't open the chest, you can run ", NamedTextColor.GRAY).append(Component.text("/claimchest", NamedTextColor.YELLOW)).append(Component.text(" next to the box.", NamedTextColor.GRAY)))
        event.entity.sendMessage(Component.text("This chest does not expire, but can be opened by anyone.", NamedTextColor.GRAY))
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.type != InventoryType.CHEST) return

        val holder = event.inventory.holder
        if (holder is DoubleChest) {
            val leftChest = holder.leftSide as? Chest ?: return
            val rightChest = holder.rightSide as? Chest ?: return

            if (!leftChest.hasMetadata(DEATH_CHEST_META)) return

            // Remove chest if empty
            if (event.inventory.isEmpty) {
                leftChest.block.type = Material.AIR
                rightChest.block.type = Material.AIR
                event.player.sendMessage(Component.text("All items have been collected.", NamedTextColor.GREEN))
            }
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
            event.player.sendMessage(Component.text("You cannot break a death chest that still contains items!", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            val item = event.item ?: return
            if (item.type == Material.EXPERIENCE_BOTTLE) {
                val meta = item.itemMeta ?: return
                val data = meta.persistentDataContainer

                if (data.has(xpKey, PersistentDataType.INTEGER)) {
                    val storedXp = data.get(xpKey, PersistentDataType.INTEGER) ?: return
                    val player: Player = event.player
                    player.giveExp(storedXp)
                    event.player.sendMessage(Component.text("You have received $storedXp XP.", NamedTextColor.GREEN))
                    item.amount -= 1
                    event.isCancelled = true
                }
            }
        }
    }
}