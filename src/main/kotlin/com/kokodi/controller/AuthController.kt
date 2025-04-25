package com.kokodi.controller

import com.kokodi.dto.LoginRequest
import com.kokodi.dto.RegisterRequest
import com.kokodi.dto.AuthResponse
import com.kokodi.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Endpoints для аутентификации")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Регистрация нового пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешная регистрация"),
            ApiResponse(responseCode = "400", description = "Логин уже занят или невалидные данные")
        ]
    )
    @PostMapping("/register")
    fun register(
        @RequestBody @Valid request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @Operation(
        summary = "Вход в систему",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный вход"),
            ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
        ]
    )
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
} 