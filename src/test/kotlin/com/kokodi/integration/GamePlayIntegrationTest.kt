package com.kokodi.integration

import com.kokodi.model.*
import com.kokodi.repository.CardRepository
import com.kokodi.repository.GameSessionRepository
import com.kokodi.repository.UserRepository
import com.kokodi.service.GameService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
@Testcontainers
class GamePlayIntegrationTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.datasource.driver-class-name") { postgres.driverClassName }
        }
    }

    @Autowired
    private lateinit var gameService: GameService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var gameSessionRepository: GameSessionRepository

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setup() {
        logger.info("Cleaning database before test")
        jdbcTemplate.update("TRUNCATE TABLE turn, game_session_deck, game_session, card, users RESTART IDENTITY CASCADE")
        logger.info("Database cleaned")
    }

    @Test
    @Transactional
    fun `test action card steal points effect`() {
        // given
        logger.info("Creating players")
        val player1 = userRepository.saveAndFlush(User(login = "player1", password = "pass", name = "Player 1"))
        val player2 = userRepository.saveAndFlush(User(login = "player2", password = "pass", name = "Player 2"))
        val player1Id = requireNotNull(player1.id)
        val player2Id = requireNotNull(player2.id)

        logger.info("Creating game")
        val gameState = gameService.createGame(player1Id)
        val gameId = requireNotNull(gameState.id)
        gameService.joinGame(gameId, player2Id)
        gameService.startGame(gameId, player1Id)

        logger.info("Setting game state")
        val game = gameSessionRepository.findById(gameId).orElseThrow()
        game.playerPoints[player2Id] = 10
        game.currentPlayerIndex = 0
        game.status = GameStatus.IN_PROGRESS

        logger.info("Finding or saving Steal card")
        val stealCard = cardRepository.findByName("Steal 2-1")
            ?: cardRepository.saveAndFlush(Card(name = "Steal 2-1", type = CardType.ACTION, value = 2))
        logger.info("Using Steal card with id: ${stealCard.id}")

        game.deck.clear()
        game.deck.add(stealCard)
        logger.info("Saving game with deck containing card id: ${stealCard.id}")
        gameSessionRepository.saveAndFlush(game)

        // when
        logger.info("Executing makeTurn for game $gameId and player $player1Id")
        gameService.makeTurn(gameId, player1Id)

        // then
        logger.info("Verifying game state")
        val finalGame = gameSessionRepository.findById(gameId).orElseThrow()
        val player1Points = finalGame.playerPoints[player1Id] ?: 0
        val player2Points = finalGame.playerPoints[player2Id] ?: 0

        assertTrue(player1Points > 0, "Player 1 should have gained points")
        assertTrue(player2Points < 10, "Player 2 should have lost points")
        assertEquals(10, player1Points + player2Points, "Total points should remain the same")
        assertEquals(GameStatus.IN_PROGRESS, finalGame.status, "Game should still be in progress")
    }

    @Test
    @Transactional
    fun `test points card effect`() {
        // given
        logger.info("Creating players")
        val player1 = userRepository.saveAndFlush(User(login = "player1", password = "pass", name = "Player 1"))
        val player2 = userRepository.saveAndFlush(User(login = "player2", password = "pass", name = "Player 2"))
        val player1Id = requireNotNull(player1.id)
        val player2Id = requireNotNull(player2.id)

        logger.info("Creating game")
        val gameState = gameService.createGame(player1Id)
        val gameId = requireNotNull(gameState.id)
        gameService.joinGame(gameId, player2Id)
        gameService.startGame(gameId, player1Id)

        logger.info("Setting game state")
        val game = gameSessionRepository.findById(gameId).orElseThrow()
        game.playerPoints[player1Id] = 5
        game.currentPlayerIndex = 0
        game.status = GameStatus.IN_PROGRESS

        logger.info("Finding or saving Points card")
        val pointsCard = cardRepository.findByName("Points Card 3-1")
            ?: cardRepository.saveAndFlush(Card(name = "Points Card 3-1", type = CardType.POINTS, value = 3))
        logger.info("Using Points card with id: ${pointsCard.id}")

        game.deck.clear()
        game.deck.add(pointsCard)
        logger.info("Saving game with deck containing card id: ${pointsCard.id}")
        gameSessionRepository.saveAndFlush(game)

        // when
        logger.info("Executing makeTurn for game $gameId and player $player1Id")
        gameService.makeTurn(gameId, player1Id)

        // then
        logger.info("Verifying game state")
        val finalGame = gameSessionRepository.findById(gameId).orElseThrow()
        val player1Points = finalGame.playerPoints[player1Id] ?: 0

        assertTrue(player1Points > 5, "Player 1 should have gained points")
        assertEquals(GameStatus.IN_PROGRESS, finalGame.status, "Game should still be in progress")
    }

    @Test
    @Transactional
    fun `test game win condition`() {
        // given
        logger.info("Creating players")
        val player1 = userRepository.saveAndFlush(User(login = "player1", password = "pass", name = "Player 1"))
        val player2 = userRepository.saveAndFlush(User(login = "player2", password = "pass", name = "Player 2"))
        val player1Id = requireNotNull(player1.id)
        val player2Id = requireNotNull(player2.id)

        logger.info("Creating game")
        val gameState = gameService.createGame(player1Id)
        val gameId = requireNotNull(gameState.id)
        gameService.joinGame(gameId, player2Id)
        gameService.startGame(gameId, player1Id)

        logger.info("Setting game state")
        val game = gameSessionRepository.findById(gameId).orElseThrow()
        game.playerPoints[player1Id] = 29
        game.currentPlayerIndex = 0
        game.status = GameStatus.IN_PROGRESS

        logger.info("Finding or saving Points card")
        val pointsCard = cardRepository.findByName("Points Card 2-1")
            ?: cardRepository.saveAndFlush(Card(name = "Points Card 2-1", type = CardType.POINTS, value = 2))
        logger.info("Using Points card with id: ${pointsCard.id}")

        game.deck.clear()
        game.deck.add(pointsCard)
        logger.info("Saving game with deck containing card id: ${pointsCard.id}")
        gameSessionRepository.saveAndFlush(game)

        // when
        logger.info("Executing makeTurn for game $gameId and player $player1Id")
        gameService.makeTurn(gameId, player1Id)

        // then
        logger.info("Verifying game state")
        val finalGame = gameSessionRepository.findById(gameId).orElseThrow()
        assertEquals(GameStatus.FINISHED, finalGame.status, "Game should be finished")
        val winner = requireNotNull(finalGame.winner)
        assertEquals(player1Id, winner.id, "Player 1 should be the winner")
    }
}