package dev.edwnl.macSMPCore.scoreboard

import dev.edwnl.macSMPCore.stats.StatsManager
import dev.edwnl.macSMPCore.utils.NumberFormatter
import fr.mrmicky.fastboard.FastBoard
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ScoreboardManager private constructor() {
    private lateinit var plugin: JavaPlugin
    private val boards = ConcurrentHashMap<UUID, FastBoard>()

    companion object {
        @Volatile
        private var instance: ScoreboardManager? = null

        fun getInstance(): ScoreboardManager {
            return instance ?: synchronized(this) {
                instance ?: ScoreboardManager().also { instance = it }
            }
        }
    }

    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm a").apply {
        timeZone = TimeZone.getTimeZone("Australia/Melbourne")
    }

    fun initialize(plugin: JavaPlugin) {
        if (this::plugin.isInitialized) return

        this.plugin = plugin

        plugin.server.pluginManager.registerEvents(ScoreboardListener(), plugin)

        // Start update task - every second
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            updateAllBoards()
        }, 20L, 20L)
    }

    private fun updateAllBoards() {
        boards.forEach { (uuid, board) ->
            plugin.server.getPlayer(uuid)?.let { player ->
                updateBoard(player, board)
            }
        }
    }

    fun createBoard(player: Player) {
        val board = FastBoard(player)
        board.updateTitle("§e§lMAC PROJECTS")
        boards[player.uniqueId] = board
        updateBoard(player, board)
    }

    private fun updateBoard(player: Player, board: FastBoard) {
        val stats = StatsManager.getInstance().getStats(player) ?: return

        val lines = mutableListOf<String>()

        // Add lines in order
        lines.add("§7${dateFormatter.format(Date())}")
        lines.add("")
        lines.add("§fName: §a${player.name}")
        lines.add("")

        // Stats with rankings
        lines.add("§fPlaytime: §a${NumberFormatter.formatSeconds(stats.playtime)} §f(#§e${stats.rankings.playtimeRank}§f)")
        lines.add("§fAchievements: §a${stats.achievements} §f(#§e${stats.rankings.achievementsRank}§f)")
        lines.add("")
        lines.add("§fDeaths: §a${stats.deaths} §f(#§e${stats.rankings.deathsRank}§f)")
        lines.add("§fKills: §a${stats.kills} §f(#§e${stats.rankings.killsRank}§f)")
        lines.add("§fBlocks: §a${stats.blocks} §f(#§e${stats.rankings.blocksRank}§f)")
        lines.add("§fDistance: §a${NumberFormatter.formatDistance(stats.distance)} §f(#§e${stats.rankings.distanceRank}§f)")
        lines.add("")
        lines.add("§emonashcoding.com")

        board.updateLines(lines)
    }

    fun removeBoard(player: Player) {
        boards.remove(player.uniqueId)?.delete()
    }

    // Cleanup on disable
    fun cleanup() {
        boards.values.forEach { it.delete() }
        boards.clear()
    }

    inner class ScoreboardListener : Listener {
        private val scoreboardManager = getInstance()

        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            scoreboardManager.createBoard(event.player)
        }

        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            scoreboardManager.removeBoard(event.player)
        }
    }
}