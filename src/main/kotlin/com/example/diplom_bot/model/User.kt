package com.example.diplom_bot.model

data class User(
    val chatId: Long,
    var state: State
) {
    enum class State {
        WRITING_DESCRIPTION,
        OTHER
    }
}
