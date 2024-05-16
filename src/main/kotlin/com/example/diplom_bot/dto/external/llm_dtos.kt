package com.example.diplom_bot.dto.external

data class LLMChatRequest(
    val model: String,
    val messages: List<LLMChatMessage>
)

data class LLMChatMessage(
    val role: String,
    val content: String
)

data class LLMChatResponse(
    val choices: List<LLMChatChoice>
)

data class LLMChatChoice(
    val index: Int,
    val message: LLMChatMessage
)
