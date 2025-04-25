package com.kokodi.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @NotBlank
    @Size(min = 3, message = "Login must be at least 3 characters long")
    @Column(unique = true, nullable = false)
    val login: String,
    
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    val password: String,
    
    @NotBlank
    @Column(nullable = false)
    val name: String
)