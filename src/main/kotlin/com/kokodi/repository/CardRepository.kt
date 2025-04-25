package com.kokodi.repository

import com.kokodi.model.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, Long> {
    fun findByName(name: String): Card?
}