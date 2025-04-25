package com.kokodi.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class LoginRequest(
    @field:NotBlank(message = "Login is required")
    val login: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String
)

data class RegisterRequest(
    @field:NotBlank(message = "Login is required")
    @field:Size(min = 3, message = "Login must be at least 3 characters")
    val login: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Name is required")
    val name: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer"
) 