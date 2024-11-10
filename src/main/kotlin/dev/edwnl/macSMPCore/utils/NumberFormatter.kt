package dev.edwnl.macSMPCore.utils

object NumberFormatter {
    fun formatNumber(number: Number): String {
        return when (val num = number.toLong()) {
            in 0..999 -> num.toString()
            in 1_000..999_999 -> String.format("%.1fk", num / 1000.0)
            in 1_000_000..999_999_999 -> String.format("%.1fm", num / 1_000_000.0)
            else -> String.format("%.1fb", num / 1_000_000_000.0)
        }
    }

    fun formatPlaytime(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours == 0L -> "${mins}m"
            mins == 0L -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
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
}