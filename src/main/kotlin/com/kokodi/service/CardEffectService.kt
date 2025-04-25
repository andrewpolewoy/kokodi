package com.kokodi.service

import com.kokodi.common.GameConstants
import com.kokodi.exception.InvalidCardException
import com.kokodi.model.Card
import com.kokodi.model.CardType
import com.kokodi.model.GameSession
import com.kokodi.model.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CardEffectService {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun applyEffect(game: GameSession, card: Card, currentPlayer: User): EffectResult {
        return when (card.type) {
            CardType.POINTS -> applyPointsEffect(game, card, currentPlayer)
            CardType.ACTION -> applyActionEffect(game, card, currentPlayer)
        }
    }

    private fun applyPointsEffect(game: GameSession, card: Card, player: User): EffectResult {
        val playerId = player.id ?: throw IllegalStateException("Player must be persisted")
        val currentPoints = game.playerPoints.getOrDefault(playerId, 0)
        val newPoints = currentPoints + card.value
        game.playerPoints[playerId] = newPoints

        logger.debug("Applied points effect: +${card.value} for player $playerId")
        return EffectResult(
            effect = "Added ${card.value} points",
            pointsChanged = true,
            newPoints = newPoints
        )
    }

    private fun applyActionEffect(game: GameSession, card: Card, player: User): EffectResult {
        return when {
            card.name.startsWith("Block") -> applyBlockEffect(game)
            card.name.startsWith("Steal") -> applyStealEffect(game, card, player)
            card.name.startsWith("DoubleDown") -> applyDoubleDownEffect(game, player)
            else -> throw InvalidCardException("Unknown action card: ${card.name}")
        }
    }

    private fun applyBlockEffect(game: GameSession): EffectResult {
        val nextPlayerIndex = (game.currentPlayerIndex + 1) % game.players.size
        game.skippedPlayerIndex = nextPlayerIndex
        game.currentPlayerIndex = (game.currentPlayerIndex + 2) % game.players.size

        logger.debug("Applied block effect: player $nextPlayerIndex skips turn")
        return EffectResult(
            effect = "Next player skips turn",
            turnOrderChanged = true
        )
    }

    private fun applyStealEffect(game: GameSession, card: Card, player: User): EffectResult {
        val playerId = player.id ?: throw IllegalStateException("Player must be persisted")
        val otherPlayers = game.players.filter { it.id != playerId }
        if (otherPlayers.isEmpty()) {
            return EffectResult(effect = "No players to steal from")
        }

        val targetPlayer = otherPlayers.random()
        val targetPlayerId = targetPlayer.id ?: throw IllegalStateException("Target player must be persisted")
        val targetPoints = game.playerPoints.getOrDefault(targetPlayerId, 0)
        val pointsToSteal = minOf(card.value, targetPoints)

        game.playerPoints[targetPlayerId] = targetPoints - pointsToSteal
        game.playerPoints[playerId] = game.playerPoints.getOrDefault(playerId, 0) + pointsToSteal

        logger.debug("Applied steal effect: ${pointsToSteal} points from player $targetPlayerId to $playerId")
        return EffectResult(
            effect = "Stole $pointsToSteal points from ${targetPlayer.name}",
            pointsChanged = true,
            newPoints = game.playerPoints[playerId]
        )
    }

    private fun applyDoubleDownEffect(game: GameSession, player: User): EffectResult {
        val playerId = player.id ?: throw IllegalStateException("Player must be persisted")
        val currentPoints = game.playerPoints.getOrDefault(playerId, 0)
        val newPoints = minOf(currentPoints * 2, GameConstants.MAX_POINTS)
        game.playerPoints[playerId] = newPoints

        logger.debug("Applied double down effect for player $playerId: $currentPoints -> $newPoints")
        return EffectResult(
            effect = "Doubled points to $newPoints",
            pointsChanged = true,
            newPoints = newPoints
        )
    }
}

data class EffectResult(
    val effect: String,
    val pointsChanged: Boolean = false,
    val turnOrderChanged: Boolean = false,
    val newPoints: Int? = null
)