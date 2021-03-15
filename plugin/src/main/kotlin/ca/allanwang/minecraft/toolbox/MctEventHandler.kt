package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.Mct
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

class MctEventHandler(
    private val mct: Mct,
    private val eventFlow: MutableSharedFlow<Event>
) : Listener {

    private fun emit(event: Event) {
        mct.mctScope.launch {
            eventFlow.emit(event)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerMove(event: PlayerMoveEvent) {
        emit(event)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        emit(event)
    }
}