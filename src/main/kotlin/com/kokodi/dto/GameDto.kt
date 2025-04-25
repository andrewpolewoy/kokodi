package com.kokodi.dto

import com.kokodi.model.GameSession
import com.kokodi.model.GameStatus
import java.time.LocalDateTime

data class GameStateResponse(
    val id: Long?,
    val status: GameStatus,
    val players: List<PlayerInfo>,
    val currentPlayerIndex: Int,
    val cardsInDeck: Int,
    val lastTurn: TurnInfo?,
    val winner: PlayerInfo?,
    val createdAt: LocalDateTime,
    val finishedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(game: GameSession): GameStateResponse {
            return GameStateResponse(
                id = game.id,
                status = game.status,
                players = game.players.map { player ->
                    val playerId = player.id ?: throw IllegalStateException("Player ID is null")
                    PlayerInfo.fromEntity(player, game.playerPoints[playerId] ?: 0)
                },
                currentPlayerIndex = game.currentPlayerIndex,
                cardsInDeck = game.deck.size,
                lastTurn = game.turns.lastOrNull()?.let { TurnInfo.fromEntity(it) },
                winner = game.winner?.let { winner ->
                    val winnerId = winner.id ?: throw IllegalStateException("Winner ID is null")
                    PlayerInfo.fromEntity(winner, game.playerPoints[winnerId] ?: 0)
                },
                createdAt = game.createdAt,
                finishedAt = game.finishedAt
            )
        }
    }
}