package com.kokodi.monitoring

import com.kokodi.model.GameStatus
import com.kokodi.repository.GameSessionRepository
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class GameHealthIndicator(
    private val gameRepository: GameSessionRepository
) : HealthIndicator {

    override fun health(): Health {
        try {
            val activeGames = gameRepository.countByStatus(GameStatus.IN_PROGRESS)
            val waitingGames = gameRepository.countByStatus(GameStatus.WAIT_FOR_PLAYERS)
            val finishedGames = gameRepository.countByStatus(GameStatus.FINISHED)

            return Health.up()
                .withDetail("activeGames", activeGames)
                .withDetail("waitingGames", waitingGames)
                .withDetail("finishedGames", finishedGames)
                .withDetail("totalGames", activeGames + waitingGames + finishedGames)
                .build()
        } catch (e: Exception) {
            return Health.down()
                .withException(e)
                .build()
        }
    }
} 