package dev.edwnl.macSMPCore.scoreboard

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ScoreboardCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }

        val enabled = ScoreboardManager.getInstance().toggleScoreboard(sender);
        sender.sendMessage(if (enabled) "§aScoreboard enabled!" else "§cScoreboard disabled!")
        return true
    }
}