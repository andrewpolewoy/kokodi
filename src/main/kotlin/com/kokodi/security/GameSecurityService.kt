package com.kokodi.security

import com.kokodi.exception.GameAccessDeniedException
import com.kokodi.model.GameSession
import org.springframework.stereotype.Service

@Service
class GameSecurityService {
    fun validateAccess(game: GameSession, userId: Long) {
        val gameId = game.id ?: throw IllegalStateException("Game ID is null")
        if (!game.players.any { it.id == userId }) {
            throw GameAccessDeniedException("User $userId has no access to game $gameId")
        }
    }
    
    fun validateGameOwner(game: GameSession, userId: Long) {
        val gameId = game.id ?: throw IllegalStateException("Game ID is null")
        if (game.players.firstOrNull()?.id != userId) {
            throw GameAccessDeniedException("User $userId is not the owner of game $gameId")
        }
    }
} 