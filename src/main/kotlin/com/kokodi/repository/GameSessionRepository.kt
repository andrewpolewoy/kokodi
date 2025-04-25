package com.kokodi.repository

import com.kokodi.model.GameSession
import com.kokodi.model.GameStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameSessionRepository : JpaRepository<GameSession, Long> {
    fun findByPlayersId(playerId: Long): List<GameSession>
    fun countByStatus(status: GameStatus): Long
} 