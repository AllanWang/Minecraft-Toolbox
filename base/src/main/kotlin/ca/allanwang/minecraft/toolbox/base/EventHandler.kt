package ca.allanwang.minecraft.toolbox.base

import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

interface MctPlayerMoveHandler {
    fun onPlayerMove(event: PlayerMoveEvent)
}

interface MctPlayerInteractionHandler {
    fun onPlayerInteract(event: PlayerInteractEvent)
}