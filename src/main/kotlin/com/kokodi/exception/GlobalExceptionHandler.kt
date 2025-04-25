package com.kokodi.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val message: String,
    val status: Int,
    val error: String,
    val timestamp: Long = System.currentTimeMillis()
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException::class)
    fun handleGameNotFound(ex: GameNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(
            message = ex.message ?: "Game not found",
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found"
        ))

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(
            message = ex.message ?: "User not found",
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found"
        ))

    @ExceptionHandler(GameRuleViolationException::class)
    fun handleGameRuleViolation(ex: GameRuleViolationException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.message ?: "Game rule violation",
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request"
        ))

    @ExceptionHandler(InvalidGameStateException::class)
    fun handleInvalidGameState(ex: InvalidGameStateException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.message ?: "Invalid game state",
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request"
        ))

    @ExceptionHandler(NotYourTurnException::class)
    fun handleNotYourTurn(ex: NotYourTurnException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.message ?: "Not your turn",
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request"
        ))

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException) = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse(
            message = ex.message ?: "User already exists",
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict"
        ))

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException) = ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse(
            message = "Invalid username or password",
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized"
        ))

    @ExceptionHandler(GameAccessDeniedException::class)
    fun handleGameAccessDenied(ex: GameAccessDeniedException) = ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse(
            message = ex.message ?: "Access denied to game",
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden"
        ))

    @ExceptionHandler(PlayerNotFoundException::class)
    fun handlePlayerNotFound(ex: PlayerNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(
            message = ex.message ?: "Player not found",
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found"
        ))

    @ExceptionHandler(InvalidCardException::class)
    fun handleInvalidCard(ex: InvalidCardException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.message ?: "Invalid card",
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request"
        ))

    @ExceptionHandler(CardEffectException::class)
    fun handleCardEffect(ex: CardEffectException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.message ?: "Card effect error",
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request"
        ))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
            message = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" },
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error"
        ))

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception) = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse(
            message = "Internal server error: ${ex.message}",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error"
        ))
} 