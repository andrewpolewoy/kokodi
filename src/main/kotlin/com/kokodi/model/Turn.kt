package com.kokodi.model

import jakarta.persistence.*

@Entity
class Turn(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(optional = false)
    val game: GameSession,

    @ManyToOne(optional = false)
    val player: User,

    @ManyToOne(optional = false)
    val card: Card,

    @Column(nullable = false)
    val effect: String,

    val pointsChanged: Boolean = false,

    val turnOrderChanged: Boolean = false,

    val newPoints: Int? = null,
)