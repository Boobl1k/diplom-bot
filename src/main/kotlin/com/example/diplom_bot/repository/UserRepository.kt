package com.example.diplom_bot.repository

import com.example.diplom_bot.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByChatId(chatId: Long): User?
}
