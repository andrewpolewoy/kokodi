package com.kokodi.service

import com.kokodi.common.GameConstants
import com.kokodi.dto.GameStateResponse
import com.kokodi.exception.*
import com.kokodi.model.*
import com.kokodi.monitoring.GameMetrics
import com.kokodi.repository.GameSessionRepository
import com.kokodi.repository.UserRepository
import com.kokodi.security.GameSecurityService
import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class GameService(
    private val gameSessionRepository: GameSessionRepository,
    private val userRepository: UserRepository,
    private val cardService: CardService,
    private val cardEffectService: CardEffectService,
    private val securityService: GameSecurityService,
    private val gameMetrics: GameMetrics
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Timed("game.create")
    @Transactional
    fun createGame(creatorId: Long): GameStateResponse {
        logger.info("Creating new game for user $creatorId")

        val creator = findUser(creatorId)
        val game = GameSession(
            status = GameStatus.WAIT_FOR_PLAYERS,
            players = mutableListOf(creator),
            createdAt = LocalDateTime.now()
        )

        val savedGame = gameSessionRepository.save(game)
        gameMetrics.recordGameCreated()

        logger.info("Created game ${savedGame.id} for user $creatorId")
        return savedGame.toGameStateResponse()
    }

    @Timed("game.turn")
    @Transactional
    fun makeTurn(gameId: Long, playerId: Long): GameStateResponse {
        logger.info("Player $playerId making turn in game $gameId")

        val game = findGame(gameId)
        securityService.validateAccess(game, playerId)
        validateGameState(game, GameStatus.IN_PROGRESS)
        validatePlayerTurn(game, playerId)

        if (game.deck.isEmpty()) {
            return finishGameDueToEmptyDeck(game)
        }

        val currentPlayer = game.players[game.currentPlayerIndex]
        val card = game.deck.removeFirst()

        val effectResult = cardEffectService.applyEffect(game, card, currentPlayer)
        recordTurn(game, currentPlayer, card, effectResult)

        if (isGameFinished(game)) {
            return finishGame(game)
        }

        updateTurnOrder(game, effectResult)

        val savedGame = gameSessionRepository.save(game)
        gameMetrics.recordTurnTaken()

        logger.info("Turn completed in game $gameId")
        return savedGame.toGameStateResponse()
    }

    private fun recordTurn(game: GameSession, player: User, card: Card, effectResult: EffectResult) {
        val turn = Turn(
            game = game,
            player = player,
            card = card,
            effect = effectResult.effect,
            pointsChanged = effectResult.pointsChanged,
            turnOrderChanged = effectResult.turnOrderChanged,
            newPoints = effectResult.newPoints
        )
        game.turns.add(turn)
        logger.debug("Recorded turn: $turn")
    }

    private fun updateTurnOrder(game: GameSession, effectResult: EffectResult) {
        if (effectResult.turnOrderChanged) {
            logger.debug("Turn order changed due to card effect")
            return
        }
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % game.players.size
    }

    private fun finishGameDueToEmptyDeck(game: GameSession): GameStateResponse {
        val winner = findWinner(game)
        game.status = GameStatus.FINISHED
        game.winner = winner
        game.finishedAt = LocalDateTime.now()

        gameMetrics.recordGameCompleted()
        logger.info("Game ${game.id} finished due to empty deck. Winner: ${winner?.name}")

        return gameSessionRepository.save(game).toGameStateResponse()
    }

    @Timed("game.join")
    @Transactional
    fun joinGame(gameId: Long, playerId: Long): GameStateResponse {
        logger.info("Player $playerId joining game $gameId")

        val game = findGame(gameId)
        val player = findUser(playerId)

        validateGameState(game, GameStatus.WAIT_FOR_PLAYERS)
        validateJoinGame(game, playerId)

        game.players.add(player)
        val savedGame = gameSessionRepository.save(game)
        gameMetrics.recordPlayerJoined()

        logger.info("Player $playerId joined game $gameId")
        return savedGame.toGameStateResponse()
    }

    @Timed("game.start")
    @Transactional
    fun startGame(gameId: Long, userId: Long): GameStateResponse {
        logger.info("Starting game $gameId")

        val game = findGame(gameId)
        securityService.validateGameOwner(game, userId)
        validateGameState(game, GameStatus.WAIT_FOR_PLAYERS)

        if (game.players.size < GameConstants.MIN_PLAYERS) {
            throw GameRuleViolationException("Not enough players to start the game")
        }

        game.deck.addAll(cardService.generateDeck())
        game.status = GameStatus.IN_PROGRESS

        val savedGame = gameSessionRepository.save(game)
        logger.info("Started game $gameId")
        return savedGame.toGameStateResponse()
    }

    fun getGame(gameId: Long, userId: Long): GameStateResponse {
        val game = findGame(gameId)
        securityService.validateAccess(game, userId)
        return game.toGameStateResponse()
    }

    fun getPlayerGames(playerId: Long): List<GameStateResponse> {
        val games = gameSessionRepository.findByPlayersId(playerId)
        return games.map { it.toGameStateResponse() }
    }

    private fun findGame(gameId: Long): GameSession {
        return gameSessionRepository.findById(gameId)
            .orElseThrow { GameNotFoundException(gameId) }
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
    }

    private fun validateGameState(game: GameSession, expectedStatus: GameStatus) {
        if (game.status != expectedStatus) {
            throw InvalidGameStateException(game.id, game.status.name)
        }
    }

    private fun validatePlayerTurn(game: GameSession, playerId: Long) {
        if (game.players.isEmpty()) {
            throw GameRuleViolationException("No players in game")
        }
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
            ?: throw GameRuleViolationException("Invalid player index: ${game.currentPlayerIndex}")
        if (currentPlayer.id != playerId) {
            throw NotYourTurnException(game.id, playerId)
        }
    }

    private fun validateJoinGame(game: GameSession, playerId: Long) {
        if (game.players.size >= GameConstants.MAX_PLAYERS) {
            throw GameRuleViolationException("Game is full")
        }
        if (game.players.any { it.id == playerId }) {
            throw GameRuleViolationException("Player already in game")
        }
    }

    private fun isGameFinished(game: GameSession): Boolean {
        return game.playerPoints.any { (_, points) -> points >= GameConstants.MAX_POINTS }
    }

    private fun findWinner(game: GameSession): User? {
        return game.playerPoints.maxByOrNull { it.value }?.key?.let { playerId ->
            game.players.find { it.id == playerId }
        }
    }
    private fun finishGame(game: GameSession): GameStateResponse {
        game.status = GameStatus.FINISHED
        game.winner = findWinner(game)
        game.finishedAt = LocalDateTime.now()
        gameMetrics.recordGameCompleted()

        game.turns.firstOrNull()?.let firstLet@{ firstTurn ->
            game.turns.lastOrNull()?.let lastLet@{ lastTurn ->
                val firstId = firstTurn.id ?: return@firstLet
                val lastId = lastTurn.id ?: return@lastLet
                val duration = lastId - firstId
                val gameId = game.id ?: return@firstLet
                gameMetrics.recordGameDuration(gameId, duration)
            }
        }

        return gameSessionRepository.save(game).toGameStateResponse()
    }

}
