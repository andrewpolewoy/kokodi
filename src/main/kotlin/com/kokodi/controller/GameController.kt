package com.kokodi.controller

import com.kokodi.dto.GameStateResponse
import com.kokodi.security.UserDetailsImpl
import com.kokodi.service.GameService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
@Tag(name = "Game", description = "Endpoints для управления игрой")
@SecurityRequirement(name = "bearerAuth")
class GameController(
    private val gameService: GameService
) {
    @Operation(
        summary = "Создать новую игру",
        responses = [
            ApiResponse(responseCode = "200", description = "Игра успешно создана"),
            ApiResponse(responseCode = "401", description = "Не авторизован"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    @PostMapping
    fun createGame(@AuthenticationPrincipal userDetails: UserDetailsImpl): ResponseEntity<GameStateResponse> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.createGame(userId))
    }
    
    @Operation(
        summary = "Присоединиться к игре",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно присоединился к игре"),
            ApiResponse(responseCode = "400", description = "Игра заполнена или уже началась"),
            ApiResponse(responseCode = "404", description = "Игра не найдена"),
            ApiResponse(responseCode = "401", description = "Не авторизован")
        ]
    )
    @PostMapping("/{id}/join")
    fun joinGame(
        @PathVariable @Parameter(description = "ID игры") id: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl
    ): ResponseEntity<GameStateResponse> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.joinGame(id, userId))
    }
    
    @Operation(
        summary = "Начать игру",
        responses = [
            ApiResponse(responseCode = "200", description = "Игра успешно начата"),
            ApiResponse(responseCode = "400", description = "Недостаточно игроков или игра уже началась"),
            ApiResponse(responseCode = "404", description = "Игра не найдена"),
            ApiResponse(responseCode = "403", description = "Нет прав для начала игры")
        ]
    )
    @PostMapping("/{id}/start")
    fun startGame(
        @PathVariable @Parameter(description = "ID игры") id: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl
    ): ResponseEntity<GameStateResponse> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.startGame(id, userId))
    }
    
    @Operation(
        summary = "Сделать ход",
        responses = [
            ApiResponse(responseCode = "200", description = "Ход выполнен успешно"),
            ApiResponse(responseCode = "400", description = "Не ваш ход или игра завершена"),
            ApiResponse(responseCode = "404", description = "Игра не найдена"),
            ApiResponse(responseCode = "403", description = "Нет прав для хода")
        ]
    )
    @PostMapping("/{id}/turn")
    fun makeTurn(
        @PathVariable @Parameter(description = "ID игры") id: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl
    ): ResponseEntity<GameStateResponse> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.makeTurn(id, userId))
    }
    
    @Operation(
        summary = "Получить состояние игры",
        responses = [
            ApiResponse(responseCode = "200", description = "Состояние игры получено"),
            ApiResponse(responseCode = "404", description = "Игра не найдена"),
            ApiResponse(responseCode = "403", description = "Нет доступа к игре")
        ]
    )
    @GetMapping("/{id}")
    fun getGame(
        @PathVariable @Parameter(description = "ID игры") id: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl
    ): ResponseEntity<GameStateResponse> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.getGame(id, userId))
    }
    
    @Operation(
        summary = "Получить список игр пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Список игр получен"),
            ApiResponse(responseCode = "401", description = "Не авторизован")
        ]
    )
    @GetMapping
    fun getPlayerGames(@AuthenticationPrincipal userDetails: UserDetailsImpl): ResponseEntity<List<GameStateResponse>> {
        val userId = userDetails.user.id ?: throw IllegalStateException("User ID is null")
        return ResponseEntity.ok(gameService.getPlayerGames(userId))
    }
} 