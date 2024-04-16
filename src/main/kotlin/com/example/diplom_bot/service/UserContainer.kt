package com.example.diplom_bot.service

import com.example.diplom_bot.model.User
import org.springframework.stereotype.Component

@Component
class UserContainer {
    private val users = mutableListOf<User>()

    fun findUser(chatId: Long): User? {
        return users.find { it.chatId == chatId }
    }

    fun findOrAdd(chatId: Long): User {
        return findUser(chatId) ?: addUser(chatId)
    }

    fun addUser(chatId: Long): User {
        val user = User(chatId, User.State.OTHER)
        users.add(user)
        return user
    }

    fun isUserWritingDescription(chatId: Long): Boolean {
        return findOrAdd(chatId).state == User.State.WRITING_DESCRIPTION
    }

    fun setUserState(chatId: Long, state: User.State) {
        findOrAdd(chatId).state = state
    }
}
