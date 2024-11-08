package dev.edwnl.macSMPCore.database.models

import org.bson.types.ObjectId
import java.util.Date

data class WhitelistedPlayer(
    val id: ObjectId,
    val minecraftUsername: String,
    val minecraftUuid: String,
    val addedByDiscordId: String,
    val addedByUsername: String,
    val addedByDisplayName: String,
    val addedAt: Date,
    val isActive: Boolean
)