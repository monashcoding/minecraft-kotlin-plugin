package dev.edwnl.macSMPCore.utils

import io.papermc.paper.advancement.AdvancementDisplay
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location

class Utils {
    companion object {
        fun formatAdvancementDisplay(display: AdvancementDisplay?): String {
            if (display == null) return "No advancement display data available"
            return PlainTextComponentSerializer.plainText().serialize(display.title());
        }

        fun formatLocation(location: Location): String {
            return "x: ${location.blockX}, y: ${location.blockY}, z: ${location.blockZ}"
        }

        fun formatSeconds(seconds: Long): String {
            if (seconds < 60) return "${seconds}s"

            val minutes = seconds / 60
            if (minutes < 60) return "${minutes}m"

            val hours = minutes / 60
            if (hours < 24) {
                val remainingMinutes = minutes % 60
                return if (remainingMinutes > 0) "${hours}h ${remainingMinutes}m" else "${hours}h"
            }

            val days = hours / 24
            val remainingHours = hours % 24
            return when {
                remainingHours > 0 -> "${days}d ${remainingHours}h"
                else -> "${days}d"
            }
        }

        fun formatDistance(meters: Long): String {
            return when {
                meters < 1000 -> "${meters}m"
                meters < 1_000_000 -> String.format("%.1fkm", meters / 1000.0)
                else -> String.format("%.1fMkm", meters / 1_000_000.0)
            }
        }

        fun formatStats(number: Long): String {
            return when {
                number < 1000 -> number.toString()
                number < 1_000_000 -> String.format("%.1fk", number / 1000.0)
                number < 1_000_000_000 -> String.format("%.1fM", number / 1_000_000.0)
                else -> String.format("%.1fB", number / 1_000_000_000.0)
            }
        }
    }
}