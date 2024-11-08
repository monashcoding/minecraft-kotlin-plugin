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
}