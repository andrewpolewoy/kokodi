package com.kokodi.dto

import com.kokodi.model.User

data class PlayerInfo(
    val id: Long?,
    val name: String,
    val points: Int
) {
    companion object {
        fun fromEntity(user: User, points: Int) = PlayerInfo(
            id = user.id,
            name = user.name,
            points = points
        )
    }
}