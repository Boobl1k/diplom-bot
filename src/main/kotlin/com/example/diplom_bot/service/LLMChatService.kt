package com.example.diplom_bot.service

import com.example.diplom_bot.client.LLMChatClient
import com.example.diplom_bot.dto.external.LLMChatMessage
import com.example.diplom_bot.dto.external.LLMChatRequest
import com.example.diplom_bot.dto.external.LLMChatResponse
import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.model.LLMProblemTypeResponse
import com.example.diplom_bot.model.LLMQuestionResponse
import com.example.diplom_bot.model.LLMResponse
import com.example.diplom_bot.model.LLMResponseContent
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.DisProblemRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class LLMChatService(
    private val llmChatClient: LLMChatClient,
    @Value("classpath:llm-context.json")
    private val llmContextResource: Resource,
    private val objectMapper: ObjectMapper,
    private val chatBotProperties: ChatBotProperties,
    disProblemRepository: DisProblemRepository
) {
    companion object : KLogging() {
        const val BAD_RESPONSE_TEXT = "Мне не удалось определить вид Вашей проблемы. Пожалуйста, попробуйте еще раз"
    }

    private val context: List<LLMChatMessage> = objectMapper.readValue(llmContextResource.inputStream)

    private val allProblemTypes = disProblemRepository.findAll()

    private val historyMap = mutableMapOf<Long, MutableList<LLMChatMessage>>()

    fun sendMessage(chatId: Long, message: String): LLMResponse {
        if (!historyMap.containsKey(chatId)) {
            historyMap[chatId] = mutableListOf<LLMChatMessage>().apply { addAll(context) }
        }

        val history = historyMap[chatId]!!
        history += LLMChatMessage("user", message)

        val response = getCompletions(LLMChatRequest(chatBotProperties.llmModelName, history))

        return response.llmChatContent?.let { llmChatContent ->
            if (llmChatContent.problemType != null) {
                getProblemTypeFromString(llmChatContent.problemType)?.let { LLMProblemTypeResponse(it) }
                    ?: LLMQuestionResponse(BAD_RESPONSE_TEXT)
            } else llmChatContent.question?.let { LLMQuestionResponse(it) }
                ?: LLMQuestionResponse(BAD_RESPONSE_TEXT)
        } ?: LLMQuestionResponse(BAD_RESPONSE_TEXT)
    }

    fun sendNotMyProblem(chatId: Long): LLMResponse {
        return sendMessage(
            chatId,
            "ты неправильно определил вид проблемы или услуги. Задай пользователю короткий уточняющий вопрос"
        )
    }

    fun regenerate(chatId: Long): LLMResponse {
        val history = historyMap[chatId]!!
        if (history.last().role == "assistant") {
            history.removeLast()
        }
        val last = history.last()
        history.removeLast()
        return sendMessage(chatId, last.content)
    }

    fun clearHistory(chatId: Long) {
        historyMap.remove(chatId)
    }

    private fun getCompletions(llmChatRequest: LLMChatRequest): LLMChatResponse {
        val completions = llmChatClient.postCompletions(llmChatRequest)
        println(completions.choices[0].message.content)
        return completions
    }

    private val LLMChatResponse.llmChatContent
        get() = run {
            try {
                objectMapper.readValue<LLMResponseContent>(this.choices[0].message.content)
            } catch (e: Exception) {
                logger.warn { e }
                null
            }
        }

    private fun getProblemTypeFromString(str: String?): DisProblem? {
        return str?.let {
            allProblemTypes.firstOrNull {
                it.llmName != null &&
                        (str.contains(it.llmName!!, ignoreCase = true) || it.llmName!!.contains(str, ignoreCase = true))
            }
        }
    }
}
