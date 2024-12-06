package dev.edwnl.macSMPCore.deathbox

import dev.edwnl.macSMPCore.MacSMPCore
import dev.edwnl.macSMPCore.listeners.DeathChestListener
import dev.edwnl.macSMPCore.utils.Utils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.type.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType

class DeathChestUtils {
    companion object {
        private val deathChestKey = NamespacedKey(MacSMPCore.instance, "death_chest")

        fun createDeathSkull(player: Player, deathMessage: Component?): ItemStack {
            val skull = ItemStack(Material.PLAYER_HEAD)
            val meta = skull.itemMeta as SkullMeta

            meta.displayName(Component.text("${player.name}'s Death", NamedTextColor.RED))
            meta.owningPlayer = player

            val lore = listOf(
                Component.text("Died on: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", NamedTextColor.GRAY),
                Component.text("Cause: ").append(deathMessage ?: Component.text("Unknown")).color(NamedTextColor.GRAY)
            )
            meta.lore(lore)

            skull.itemMeta = meta
            return skull
        }

        fun createDoubleChest(location: Location, player: Player, plugin: MacSMPCore): Inventory {
            val block1 = location.block
            val block2 = location.clone().add(1.0, 0.0, 0.0).block

            // Set both blocks to chests
            block1.type = Material.CHEST
            block2.type = Material.CHEST

            // Set both persistent data and metadata for backward compatibility
            val chest1 = block1.state as org.bukkit.block.Chest
            val chest2 = block2.state as org.bukkit.block.Chest

            chest1.persistentDataContainer.set(deathChestKey, PersistentDataType.BYTE, 1)
            chest2.persistentDataContainer.set(deathChestKey, PersistentDataType.BYTE, 1)

            chest1.update()
            chest2.update()

            // Configure the chest data to form a double chest
            val chestData1 = block1.blockData as Chest
            val chestData2 = block2.blockData as Chest

            chestData1.type = Chest.Type.LEFT
            chestData2.type = Chest.Type.RIGHT

            // Ensure both chests face the same direction
            chestData1.facing = chestData2.facing

            // Apply the chest data to the blocks
            block1.blockData = chestData1
            block2.blockData = chestData2

            val chestState = block1.state as org.bukkit.block.Chest
            chestState.customName(Component.text("${player.name}'s death chest"))
            chestState.update()

            return (chestState.inventory.holder as DoubleChest).inventory
        }

        fun isDeathChest(chest: org.bukkit.block.Chest): Boolean {
            return chest.persistentDataContainer.has(deathChestKey, PersistentDataType.BYTE)
        }

        fun findValidDoubleChestLocation(location: Location, player: Player): Location {
            val original = location.clone().block.location

            if (original.world != null && original.world.environment == World.Environment.THE_END) {
                if (original.y < 0) {
                    player.sendMessage(Component.text()
                        .append(Component.text("It looks like you died to the void! We have moved your deathbox up.", NamedTextColor.RED))
                        .build())
                    original.set(original.x, 70.0, original.z)
                }
            }

            // Check original location and space to the right
            if (isValidDoubleChestLocation(original)) return original

            // Check adjacent blocks in a spiral pattern
            val directions = listOf(
                BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN
            )

            for (direction in directions) {
                val newLoc = original.block.getRelative(direction).location
                if (isValidDoubleChestLocation(newLoc)) return newLoc
            }

            // If no valid location found, force the original location
            return original
        }

        fun isValidDoubleChestLocation(location: Location): Boolean {
            val block1 = location.block
            val block2 = block1.getRelative(BlockFace.EAST)
            return (block1.type == Material.AIR || block1.type == Material.CAVE_AIR) &&
                    (block2.type == Material.AIR || block2.type == Material.CAVE_AIR)
        }
    }
}