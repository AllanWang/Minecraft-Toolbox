package ca.allanwang.minecraft.toolbox.base

import org.bukkit.event.player.PlayerMoveEvent

interface MctPlayerMoveHandler {
    fun onPlayerMove(event: PlayerMoveEvent)
}