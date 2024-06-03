package com.example.diplom_bot.model

data class LLMResponseContent(
    val problemType: String?,
    val question: String?,
    val explanationOfProblemType: String?,
    val explanationOfQuestion: String?
)
