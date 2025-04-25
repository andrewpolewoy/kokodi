package com.kokodi.monitoring

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GameMetrics(
    private val registry: MeterRegistry
) {
    private val gameCreatedCounter = registry.counter("game.created")
    private val gameCompletedCounter = registry.counter("game.completed")
    private val playerJoinedCounter = registry.counter("game.player.joined")
    private val turnTakenCounter = registry.counter("game.turn.taken")
    private val gameDurationTimer = registry.timer("game.duration")
    private val turnDurationTimer = registry.timer("game.turn.duration")

    fun recordGameCreated() {
        gameCreatedCounter.increment()
    }

    fun recordGameCompleted() {
        gameCompletedCounter.increment()
    }

    fun recordPlayerJoined() {
        playerJoinedCounter.increment()
    }

    fun recordTurnTaken() {
        turnTakenCounter.increment()
    }

    fun recordGameDuration(gameId: Long, durationMs: Long) {
        gameDurationTimer.record(durationMs, TimeUnit.MILLISECONDS)
        registry.gauge("game.duration.last", durationMs)
    }

    fun recordTurnDuration(action: () -> Unit) {
        turnDurationTimer.record(Runnable { action() })
    }
}