package com.example.diplom_bot.model

import com.example.diplom_bot.entity.DisProblem

sealed class LLMResponse

data class LLMQuestionResponse(val question: String) : LLMResponse()

data class LLMProblemTypeResponse(val disProblem: DisProblem) : LLMResponse()
