//package com.kokodi.controller
//
//import com.kokodi.dto.GameStateResponse
//import com.kokodi.model.GameStatus
//import com.kokodi.security.UserDetailsImpl
//import com.kokodi.service.GameService
//import com.ninjasquad.springmockk.MockkBean
//import io.mockk.every
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.http.MediaType
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//
//@WebMvcTest(GameController::class)
//class GameControllerTest {
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockkBean
//    private lateinit var gameService: GameService
//
//    @Test
//    @WithMockUser
//    fun `create game should return game state`() {
//        // given
//        val userId = 1L
//        val gameState = GameStateResponse(
//            id = 1,
//            status = GameStatus.WAIT_FOR_PLAYERS,
//            players = emptyList(),
//            currentPlayerIndex = 0,
//            cardsInDeck = 23,
//            lastTurn = null
//        )
//
//        every { gameService.createGame(userId) } returns gameState
//
//        // when/then
//        mockMvc.perform(post("/api/games")
//            .with(jwt().jwt { it.claim("userId", userId) })
//            .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.id").value(1))
//            .andExpect(jsonPath("$.status").value("WAIT_FOR_PLAYERS"))
//            .andExpect(jsonPath("$.players").isEmpty)
//            .andExpect(jsonPath("$.currentPlayerIndex").value(0))
//            .andExpect(jsonPath("$.cardsInDeck").value(23))
//    }
//
//    @Test
//    @WithMockUser
//    fun `join game should return updated game state`() {
//        // given
//        val gameId = 1L
//        val userId = 2L
//        val gameState = GameStateResponse(
//            id = gameId,
//            status = GameStatus.WAIT_FOR_PLAYERS,
//            players = listOf(User(id = 1, login = "player1", name = "Player 1")),
//            currentPlayerIndex = 0,
//            cardsInDeck = 23,
//            lastTurn = null
//        )
//
//        every { gameService.joinGame(gameId, userId) } returns gameState
//
//        // when/then
//        mockMvc.perform(post("/api/games/$gameId/join")
//            .with(jwt().jwt { it.claim("userId", userId) })
//            .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.id").value(gameId))
//            .andExpect(jsonPath("$.players").isArray)
//            .andExpect(jsonPath("$.players.length()").value(1))
//    }
//
//    @Test
//    @WithMockUser
//    fun `start game should return started game state`() {
//        // given
//        val gameId = 1L
//        val userId = 1L
//        val gameState = GameStateResponse(
//            id = gameId,
//            status = GameStatus.IN_PROGRESS,
//            players = listOf(User(id = 1, login = "player1", name = "Player 1")),
//            currentPlayerIndex = 0,
//            cardsInDeck = 23,
//            lastTurn = null
//        )
//
//        every { gameService.startGame(gameId) } returns gameState
//
//        // when/then
//        mockMvc.perform(post("/api/games/$gameId/start")
//            .with(jwt().jwt { it.claim("userId", userId) })
//            .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
//            .andExpect(jsonPath("$.currentPlayerIndex").value(0))
//    }
//
//    @Test
//    @WithMockUser
//    fun `make turn should return updated game state`() {
//        // given
//        val gameId = 1L
//        val userId = 1L
//        val gameState = GameStateResponse(
//            id = gameId,
//            status = GameStatus.IN_PROGRESS,
//            players = listOf(User(id = 1, login = "player1", name = "Player 1")),
//            currentPlayerIndex = 1,
//            cardsInDeck = 22,
//            lastTurn = Turn(id = 1, playerId = 1, cardType = "POINTS", points = 5)
//        )
//
//        every { gameService.makeTurn(gameId, userId) } returns gameState
//
//        // when/then
//        mockMvc.perform(post("/api/games/$gameId/turn")
//            .with(jwt().jwt { it.claim("userId", userId) })
//            .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.currentPlayerIndex").value(1))
//            .andExpect(jsonPath("$.cardsInDeck").value(22))
//            .andExpect(jsonPath("$.lastTurn.cardType").value("POINTS"))
//    }
//}