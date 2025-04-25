package com.kokodi.model

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Entity
class Card(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CardType,

    @NotBlank
    @Column(nullable = false, unique = true)
    val name: String,

    @Min(0)
    @Column(nullable = false)
    val value: Int
) {
    init {
        require(value >= 0) { "Card value must be non-negative" }
    }
}

enum class CardType {
    POINTS,
    ACTION
} 