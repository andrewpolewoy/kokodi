package com.kokodi.service

import com.kokodi.model.Card
import com.kokodi.model.CardType
import com.kokodi.repository.CardRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardService(
    private val cardRepository: CardRepository
) {
    companion object {
        const val POINTS_CARDS_COUNT = 10
        const val BLOCK_CARDS_COUNT = 5
        const val STEAL_CARDS_COUNT = 5
        const val DOUBLEDOWN_CARDS_COUNT = 3
        const val MIN_POINTS_VALUE = 1
        const val MAX_POINTS_VALUE = 5
        const val STEAL_MIN_VALUE = 1
        const val STEAL_MAX_VALUE = 3
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun generateDeck(): List<Card> {
        val cards = mutableListOf<Card>()

        // Points cards
        repeat(POINTS_CARDS_COUNT) { index ->
            val value = (MIN_POINTS_VALUE..MAX_POINTS_VALUE).random()
            cards.add(Card(
                type = CardType.POINTS,
                name = "Points Card $value-${index + 1}", // Уникальное имя: Points Card 3-1
                value = value
            ))
        }

        // Block cards
        repeat(BLOCK_CARDS_COUNT) { index ->
            cards.add(Card(
                type = CardType.ACTION,
                name = "Block-${index + 1}", // Уникальное имя: Block-1
                value = 1
            ))
        }

        // Steal cards
        repeat(STEAL_CARDS_COUNT) { index ->
            val value = (STEAL_MIN_VALUE..STEAL_MAX_VALUE).random()
            cards.add(Card(
                type = CardType.ACTION,
                name = "Steal $value-${index + 1}", // Уникальное имя: Steal 2-1
                value = value
            ))
        }

        // DoubleDown cards
        repeat(DOUBLEDOWN_CARDS_COUNT) { index ->
            cards.add(Card(
                type = CardType.ACTION,
                name = "DoubleDown-${index + 1}", // Уникальное имя: DoubleDown-1
                value = 2
            ))
        }

        logger.debug("Generated ${cards.size} cards for deck")
        return cardRepository.saveAll(cards.shuffled())
    }
}