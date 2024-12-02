package dev.edwnl.macSMPCore.nick

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.haoshoku.nick.api.NickAPI

// Disabled - NickAPI not compatible with 1.21.3 yet
class NickCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("/nick reset", NamedTextColor.YELLOW))
            sender.sendMessage(Component.text("/nick <Name>", NamedTextColor.YELLOW))
            return true
        }

        when (args[0].lowercase()) {
            "reset" -> {
                NickAPI.resetNick(sender)
                NickAPI.resetSkin(sender)
                NickAPI.resetUniqueId(sender)
                NickAPI.resetProfileName(sender)
                NickAPI.refreshPlayer(sender)
                sender.sendMessage(
                    Component.text("Successfully reset nick")
                        .color(NamedTextColor.GREEN)
                )
            }
            else -> {
                val name = args[0]
                NickAPI.setNick(sender, name)
                NickAPI.setSkin(sender, name)
                NickAPI.setUniqueId(sender, name)
                NickAPI.setProfileName(sender, name)
                NickAPI.refreshPlayer(sender)
                sender.sendMessage(
                    Component.text("Successfully set the nickname to ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(name).color(NamedTextColor.YELLOW))
                )
            }
        }
        return true
    }
}