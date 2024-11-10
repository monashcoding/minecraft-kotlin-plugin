package dev.edwnl.macSMPCore.utils

import io.papermc.paper.advancement.AdvancementDisplay
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class Utils {
    companion object {
        fun formatAdvancementDisplay(display: AdvancementDisplay?): String {
            if (display == null) return "No advancement display data available"
            return PlainTextComponentSerializer.plainText().serialize(display.title());

//            return buildString {
//                appendLine("=== Advancement Display ===")
//
//                // Title and Description
//                appendLine("Title: ${PlainTextComponentSerializer.plainText().serialize(display.title()) ?: "Untitled"}")
//                appendLine("Description: ${PlainTextComponentSerializer.plainText().serialize(display.displayName()) ?: "No description"}")
//
//                // Icon
//                val icon = display.icon()
//                appendLine("Icon: ${
//                    when {
//                        icon == null -> "None"
//                        icon.type.isAir -> "None"
//                        else -> "${icon.type.name} x${icon.amount}"
//                    }
//                }")
//
//                appendLine("Type: ${display.frame().name ?: "Unknown"}")
//
//                // Visibility Settings
//                appendLine("Settings:")
//                appendLine("  • Hidden: ${display.isHidden}")
//                appendLine("  • Show Toast: ${display.doesShowToast()}")
//                appendLine("  • Announce in Chat: ${display.doesAnnounceToChat()}")
//            }
        }
    }
}