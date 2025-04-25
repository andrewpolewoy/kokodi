package com.kokodi.exception

sealed class GameException(message: String) : RuntimeException(message)

class GameNotFoundException(id: Long?) :
    GameException("Game not found: $id")

class UserNotFoundException(id: Long?) :
    GameException("User not found: $id")

class GameRuleViolationException(message: String) : 
    GameException(message)

class InvalidGameStateException(gameId: Long?, status: String) :
    GameException("Game ${gameId ?: "with null ID"} is in invalid state: $status")

class NotYourTurnException(gameId: Long?, playerId: Long) :
    GameException("Not player $playerId's turn in game $gameId")

class UserAlreadyExistsException(login: String) :
    GameException("User with login $login already exists")

class PlayerNotFoundException(playerId: Long?) :
    GameException("Player not found: $playerId")

class InvalidCardException(cardName: String) :
    GameException("Invalid card: $cardName")

class CardEffectException(message: String) :
    GameException(message)

class GameAccessDeniedException(message: String) :
    GameException(message) 