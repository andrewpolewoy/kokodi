package com.kokodi.dto

import com.kokodi.model.Turn

data class TurnInfo(
    val playerId: Long?,
    val playerName: String,
    val cardName: String,
    val effect: String,
    val pointsChanged: Boolean,
    val turnOrderChanged: Boolean,
    val newPoints: Int?
) {
    companion object {
        fun fromEntity(turn: Turn) = TurnInfo(
            playerId = turn.player.id,
            playerName = turn.player.name,
            cardName = turn.card.name,
            effect = turn.effect,
            pointsChanged = turn.pointsChanged,
            turnOrderChanged = turn.turnOrderChanged,
            newPoints = turn.newPoints
        )
    }
}