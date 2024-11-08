package dev.edwnl.macSMPCore.utils

import dev.edwnl.macSMPCore.MacSMPCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!MacSMPCore.instance.isEnabled) return
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            MacSMPCore.instance.server.scheduler.runTask(
                MacSMPCore.instance,
                block
            )
        }
    }
}

val Dispatchers.Bukkit: CoroutineDispatcher
    get() = BukkitDispatcher