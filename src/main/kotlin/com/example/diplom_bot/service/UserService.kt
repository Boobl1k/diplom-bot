package com.example.diplom_bot.service

import com.example.diplom_bot.entity.User
import com.example.diplom_bot.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun find(chatId: Long): User {
        userRepository.findByChatId(chatId).run { if (this != null) return this }
        return userRepository.save(User(chatId, User.State.OTHER))
    }
}
