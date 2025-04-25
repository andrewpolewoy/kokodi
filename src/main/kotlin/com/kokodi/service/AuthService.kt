package com.kokodi.service

import com.kokodi.dto.LoginRequest
import com.kokodi.dto.RegisterRequest
import com.kokodi.dto.AuthResponse
import com.kokodi.exception.UserAlreadyExistsException
import com.kokodi.model.User
import com.kokodi.repository.UserRepository
import com.kokodi.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByLogin(request.login) != null) {
            throw UserAlreadyExistsException(request.login)
        }

        val user = User(
            login = request.login,
            password = passwordEncoder.encode(request.password),
            name = request.name
        )
        userRepository.save(user)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.login, request.password)
        )

        val token = jwtTokenProvider.generateToken(authentication)
        return AuthResponse(token)
    }

    fun login(request: LoginRequest): AuthResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.login, request.password)
        )

        val token = jwtTokenProvider.generateToken(authentication)
        return AuthResponse(token)
    }
} 