package com.example.diplom_bot.service

import com.example.diplom_bot.model.User
import org.springframework.stereotype.Component

@Component
class UserContainer {
    private val users = mutableListOf<User>()

    fun find(chatId: Long): User {
        return users.find { it.chatId == chatId } ?: User(chatId, User.State.OTHER).apply { users.add(this) }
    }
}
