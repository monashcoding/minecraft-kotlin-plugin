package dev.edwnl.macSMPCore.nick
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import xyz.haoshoku.nick.api.NickAPI

class WhoisCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(
                Component.text("/whois <player>")
                    .color(NamedTextColor.YELLOW)
            )
            return true
        }
        val targetName = args[0]
        val targetPlayer = Bukkit.getPlayer(targetName)
        if (targetPlayer == null) {
            sender.sendMessage(
                Component.text("Player not found")
                    .color(NamedTextColor.GREEN)
            )
            return true
        }
        if (!NickAPI.isNicked(targetPlayer)) {
            sender.sendMessage(
                Component.text("Player ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName).color(NamedTextColor.YELLOW))
                    .append(Component.text(" is not nicked").color(NamedTextColor.GREEN))
            )
            return true
        }
        val realName = NickAPI.getOriginalName(targetPlayer)
        sender.sendMessage(
            Component.text("Player ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(targetName).color(NamedTextColor.YELLOW))
                .append(Component.text(" is actually ").color(NamedTextColor.GREEN))
                .append(Component.text(realName).color(NamedTextColor.YELLOW))
        )
        return true
    }
}