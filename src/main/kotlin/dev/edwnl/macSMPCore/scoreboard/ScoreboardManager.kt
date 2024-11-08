// File: src/main/kotlin/dev/edwnl/macSMPCore/scoreboard/ScoreboardManager.kt
package dev.edwnl.macSMPCore.scoreboard

import dev.edwnl.macSMPCore.MacSMPCore
import dev.edwnl.macSMPCore.stats.PlayerStats
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import dev.edwnl.macSMPCore.utils.NumberFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ScoreboardManager(private val plugin: MacSMPCore) {
    // Track last update time for each player
    private val lastUpdateTime = ConcurrentHashMap<UUID, Long>()

    // Minimum time between updates in milliseconds (1 second)
    private val UPDATE_COOLDOWN = 5000L

    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm a").apply {
        timeZone = TimeZone.getTimeZone("Australia/Melbourne")
    }

    fun scheduleUpdate(player: Player, stats: PlayerStats) {
        val now = System.currentTimeMillis()
        val lastUpdate = lastUpdateTime.getOrDefault(player.uniqueId, 0L)

        if (now - lastUpdate >= UPDATE_COOLDOWN) {
            // If cooldown has elapsed, update immediately
            lastUpdateTime[player.uniqueId] = now
            createScoreboard(player, stats)
        } else {
            // Schedule update for when cooldown expires
            val delay = UPDATE_COOLDOWN - (now - lastUpdate)
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                lastUpdateTime[player.uniqueId] = System.currentTimeMillis()
                createScoreboard(player, stats)
            }, delay / 50) // Convert milliseconds to ticks (20 ticks = 1 second)
        }
    }

    // Remove player from tracking when they leave
    fun cleanup(player: Player) {
        lastUpdateTime.remove(player.uniqueId)
    }

    fun createScoreboard(player: Player, stats: PlayerStats) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective(
            "macprojects",
            Criteria.DUMMY,
            Component.text("MAC PROJECTS")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
        )

        objective.displaySlot = DisplaySlot.SIDEBAR

        var score = 1

        // Website
        objective.getScore("§emonashcoding.com").score = score++

        // Empty line
        objective.getScore("").score = score++

        // Stats with rankings
        addStatLine(objective, "Distance", stats.distance, stats.rankings.distanceRank, score++)
        addStatLine(objective, "Blocks", stats.blocks, stats.rankings.blocksRank, score++)
        addStatLine(objective, "Kills", stats.kills, stats.rankings.killsRank, score++)
        addStatLine(objective, "Deaths", stats.deaths, stats.rankings.deathsRank, score++)

        // Empty line
        objective.getScore(" ").score = score++

        // Achievements
        addStatLine(objective, "Achievements", stats.achievements, stats.rankings.achievementsRank, score++)

        // Playtime
        addPlaytimeLine(objective, stats.playtime, stats.rankings.playtimeRank, score++)

        // Player name
        objective.getScore("§fName: §a${player.name}").score = score++

        // Empty line
        objective.getScore("  ").score = score++

        // Date
        objective.getScore("§7${dateFormatter.format(Date())}").score = score

        player.scoreboard = scoreboard
    }

    private fun addStatLine(objective: Objective, name: String, value: Number, rank: Int, score: Int) {
        val formattedValue = NumberFormatter.formatNumber(value)
        objective.getScore("§f$name: §a$formattedValue §f(#§e$rank§f)").score = score
    }

    private fun addPlaytimeLine(objective: Objective, minutes: Long, rank: Int, score: Int) {
        val formattedTime = NumberFormatter.formatPlaytime(minutes)
        objective.getScore("§fPlaytime: §a$formattedTime §f(#§e$rank§f)").score = score
    }
}