package com.kokodi.service

import org.springframework.stereotype.Service
import java.util.Random

interface RandomService {
    fun nextInt(from: Int, until: Int): Int
    fun <T> pickRandom(list: List<T>): T?
}

@Service
class DefaultRandomService : RandomService {
    private val random = Random()
    
    override fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)
    override fun <T> pickRandom(list: List<T>): T? = list.randomOrNull()
} 