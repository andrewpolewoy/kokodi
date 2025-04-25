package com.kokodi.service

import com.kokodi.exception.GameRuleViolationException
import com.kokodi.exception.NotYourTurnException
import com.kokodi.exception.UserNotFoundException
import com.kokodi.model.*
import com.kokodi.repository.GameSessionRepository
import com.kokodi.repository.UserRepository
import com.kokodi.security.GameSecurityService
import com.kokodi.monitoring.GameMetrics
import io.mockk.*
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class GameServiceTest {
    private val gameSessionRepository = mockk<GameSessionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val cardService = mockk<CardService>()
    private val cardEffectService = mockk<CardEffectService>()
    private val securityService = mockk<GameSecurityService>()
    private val gameMetrics = mockk<GameMetrics>(relaxUnitFun = true)
    
    private lateinit var gameService: GameService
    
    @BeforeEach
    fun setup() {
        gameService = GameService(
            gameSessionRepository,
            userRepository,
            cardService,
            cardEffectService,
            securityService,
            gameMetrics
        )
    }
    
    @Test
    fun `create game should create new game with creator`() {
        // given
        val creator = User(id = 1, login = "player1", password = "pass", name = "Player 1")
        val expectedGame = GameSession(
            id = 1,
            players = mutableListOf(creator),
            status = GameStatus.WAIT_FOR_PLAYERS
        )
        
        every { userRepository.findById(1) } returns Optional.of(creator)
        every { gameSessionRepository.save(any()) } returns expectedGame
        
        // when
        val result = gameService.createGame(1)
        
        // then
        verify { 
            gameMetrics.recordGameCreated()
            gameSessionRepository.save(match { 
                it.players.size == 1 && 
                it.players[0].id == 1L &&
                it.status == GameStatus.WAIT_FOR_PLAYERS &&
                it.playerPoints.isEmpty() &&
                it.currentPlayerIndex == 0
            })
        }
        assertEquals(1L, result.id)
        assertEquals(1, result.players.size)
        assertEquals(creator.id, result.players[0].id)
        assertEquals(GameStatus.WAIT_FOR_PLAYERS, result.status)
    }
    
    @Test
    fun `create game should throw exception when user not found`() {
        // given
        every { userRepository.findById(1) } returns Optional.empty()
        
        // when/then
        assertThrows<UserNotFoundException>("Should throw when user not found") {
            gameService.createGame(1)
        }
    }
    
    @Test
    fun `join game should add player to existing game`() {
        // given
        val creator = User(id = 1, login = "player1", password = "pass", name = "Player 1")
        val joiningPlayer = User(id = 2, login = "player2", password = "pass", name = "Player 2")
        val game = GameSession(
            id = 1,
            players = mutableListOf(creator),
            status = GameStatus.WAIT_FOR_PLAYERS
        )
        
        every { gameSessionRepository.findById(1) } returns Optional.of(game)
        every { userRepository.findById(2) } returns Optional.of(joiningPlayer)
        every { securityService.validateAccess(game, 2) } just runs
        every { gameSessionRepository.save(any()) } returnsArgument 0
        
        // when
        val result = gameService.joinGame(1, 2)
        
        // then
        verify { 
            gameMetrics.recordPlayerJoined()
            gameSessionRepository.save(match { 
                it.players.size == 2 && 
                it.players.any { p -> p.id == 2L } &&
                it.status == GameStatus.WAIT_FOR_PLAYERS
            })
        }
        assertEquals(2, result.players.size)
        assertTrue(result.players.any { it.id == 2L })
        assertEquals(GameStatus.WAIT_FOR_PLAYERS, result.status)
    }
    
    @Test
    fun `join game should throw exception when game is full`() {
        // given
        val creator = User(id = 1, login = "player1", password = "pass", name = "Player 1")
        val player2 = User(id = 2, login = "player2", password = "pass", name = "Player 2")
        val player3 = User(id = 3, login = "player3", password = "pass", name = "Player 3")
        val player4 = User(id = 4, login = "player4", password = "pass", name = "Player 4")
        val joiningPlayer = User(id = 5, login = "player5", password = "pass", name = "Player 5")
        
        val game = GameSession(
            id = 1,
            players = mutableListOf(creator, player2, player3, player4),
            status = GameStatus.WAIT_FOR_PLAYERS
        )
        
        every { gameSessionRepository.findById(1) } returns Optional.of(game)
        every { userRepository.findById(5) } returns Optional.of(joiningPlayer)
        every { securityService.validateAccess(game, 5) } just runs
        
        // when/then
        assertThrows<GameRuleViolationException>("Should throw when game is full") {
            gameService.joinGame(1, 5)
        }
    }
    
    @Test
    fun `start game should throw exception when not enough players`() {
        // given
        val creator = User(id = 1, login = "player1", password = "pass", name = "Player 1")
        val game = GameSession(
            id = 1,
            players = mutableListOf(creator),
            status = GameStatus.WAIT_FOR_PLAYERS
        )
        
        every { gameSessionRepository.findById(1) } returns Optional.of(game)
        every { securityService.validateGameOwner(game, 1) } just runs
        
        // when/then
        assertThrows<GameRuleViolationException>("Should throw when not enough players") {
            gameService.startGame(1, 1)
        }
    }
    
    @Test
    fun `make turn should throw exception when not player's turn`() {
        // given
        val player1 = User(id = 1, login = "player1", password = "pass", name = "Player 1")
        val player2 = User(id = 2, login = "player2", password = "pass", name = "Player 2")
        val game = GameSession(
            id = 1,
            players = mutableListOf(player1, player2),
            status = GameStatus.IN_PROGRESS,
            currentPlayerIndex = 1 // It's player2's turn
        )
        
        every { gameSessionRepository.findById(1) } returns Optional.of(game)
        every { securityService.validateAccess(game, 1) } just runs
        
        // when/then
        assertThrows<NotYourTurnException>("Should throw when not player's turn") {
            gameService.makeTurn(1, 1) // player1 trying to make a turn
        }
    }
} 