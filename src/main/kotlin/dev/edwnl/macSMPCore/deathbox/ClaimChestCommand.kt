package dev.edwnl.macSMPCore.deathbox

import dev.edwnl.macSMPCore.MacSMPCore
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandExecutor

class ClaimChestCommand(private val plugin: MacSMPCore): CommandExecutor {
    private val radius = 5

    private fun findNearestDeathChest(player: Player): Chest? {
        val location = player.location
        var nearestChest: Chest? = null
        var shortestDistance = Double.MAX_VALUE

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = location.block.getRelative(x, y, z)
                    if (block.type == Material.CHEST) {
                        val state = block.state as? Chest ?: continue
                        if (!DeathChestUtils.isDeathChest(state)) continue;

                        // avoid sqrt as its inefficient
                        val distance = x * x.toDouble() + y * y.toDouble() + z * z.toDouble()
                        if (distance < shortestDistance) {
                            shortestDistance = distance
                            nearestChest = state
                        }
                    }
                }
            }
        }

        return nearestChest
    }

    private fun dropChestContents(chest: Chest, player: Player) {
        val inventory = when (val holder = chest.inventory.holder) {
            is DoubleChest -> holder.inventory
            else -> chest.inventory
        }

        val location = player.location
        inventory.contents.forEach { item ->
            if (item != null) {
                player.world.dropItemNaturally(location, item)
            }
        }
        inventory.clear()
    }

    private fun removeDeathChest(chest: Chest) {
        if (chest.inventory.holder is DoubleChest) {
            val doubleChest = chest.inventory.holder as DoubleChest
            (doubleChest.leftSide as? Chest)?.block?.type = Material.AIR
            (doubleChest.rightSide as? Chest)?.block?.type = Material.AIR
        } else {
            chest.block.type = Material.AIR
        }
    }

    override fun onCommand(
        sender: CommandSender,
        p1: org.bukkit.command.Command,
        p2: String,
        p3: Array<out String>?
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players!")
            return true;
        }

        val nearestChest = findNearestDeathChest(sender)

        if (nearestChest == null) {
            sender.sendMessage(Component.text("No death chests found within $radius blocks!", NamedTextColor.RED))
            return true;
        }

        dropChestContents(nearestChest, sender)
        removeDeathChest(nearestChest)
        sender.sendMessage(
            Component.text(
                "Successfully dropped items from the nearest death chest!",
                NamedTextColor.GREEN
            )
        )
        return true;
    }
}