package com.kokodi.model

import com.kokodi.dto.GameStateResponse
import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class GameSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    var status: GameStatus = GameStatus.WAIT_FOR_PLAYERS,

    @ManyToMany
    @Size(min = 2, max = 4)
    val players: MutableList<User> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL])
    val deck: MutableList<Card> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL])
    val turns: MutableList<Turn> = mutableListOf(),

    @ElementCollection
    val playerPoints: MutableMap<Long, Int> = mutableMapOf(),

    @Min(0)
    var currentPlayerIndex: Int = 0,

    var skippedPlayerIndex: Int? = null,

    @ManyToOne
    var winner: User? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var finishedAt: LocalDateTime? = null
)

enum class GameStatus {
    WAIT_FOR_PLAYERS,
    IN_PROGRESS,
    FINISHED
}

fun GameSession.toGameStateResponse() = GameStateResponse.fromEntity(this)