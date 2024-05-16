package com.example.diplom_bot.service

import com.example.diplom_bot.client.LLMChatClient
import com.example.diplom_bot.dto.external.LLMChatMessage
import com.example.diplom_bot.dto.external.LLMChatRequest
import com.example.diplom_bot.dto.external.LLMChatResponse
import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.model.LLMProblemTypeResponse
import com.example.diplom_bot.model.LLMQuestionResponse
import com.example.diplom_bot.model.LLMResponse
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.DisProblemRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
class LLMChatService(
    private val llmChatClient: LLMChatClient,
    @Value("classpath:llm-context.json")
    private val llmContextResource: Resource,
    objectMapper: ObjectMapper,
    private val chatBotProperties: ChatBotProperties,
    disProblemRepository: DisProblemRepository
) {
    companion object : KLogging()

    private val context: List<LLMChatMessage> = objectMapper.readValue(llmContextResource.inputStream)

    private val allProblemTypes = disProblemRepository.findAll()

    private val historyMap = mutableMapOf<Long, MutableList<LLMChatMessage>>()

    private val threadPool = Executors.newFixedThreadPool(5)

    fun sendMessage(chatId: Long, message: String): LLMResponse {
        if (!historyMap.containsKey(chatId)) {
            historyMap[chatId] = mutableListOf<LLMChatMessage>().apply { addAll(context) }
        }

        val history = historyMap[chatId]!!
        history += LLMChatMessage("user", message)

        val responses = getCompletions(LLMChatRequest(chatBotProperties.llmModelName, history))
            .map { it.choices[0].message }

        val problemType = responses.mapNotNull { getProblemTypeFromAnswer(it.content) }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }

        if (problemType != null && problemType.value >= 2) {
            val rightResponse = responses.first {
                getProblemTypeFromAnswer(it.content) == problemType.key
            }
            history.add(rightResponse)
            return LLMProblemTypeResponse(problemType.key)
        } else {
            history.add(responses.first())
            return LLMQuestionResponse(responses.first().content)
        }
    }

    fun sendNotMyProblem(chatId: Long): LLMResponse {
        return sendMessage(
            chatId,
            "ты неправильно определил вид проблемы или услуги. Задай пользователю короткий уточняющий вопрос"
        )
    }

    fun regenerate(chatId: Long): LLMResponse {
        val history = historyMap[chatId]!!
        history.removeLast()
        val last = history.last()
        return sendMessage(chatId, last.content)
    }

    fun clearHistory(chatId: Long) {
        historyMap.remove(chatId)
    }

    private fun getCompletions(llmChatRequest: LLMChatRequest): List<LLMChatResponse> {
        val features = mutableListOf<Future<LLMChatResponse>>()
        repeat(5) {
            features.add(threadPool.submit(Callable {
                val completions = llmChatClient.postCompletions(llmChatRequest)
                println(completions.choices[0].message.content)
                completions
            }))
        }
        return features.map { it.get() }
    }

    private fun getProblemTypeFromAnswer(answer: String): DisProblem? {
        return allProblemTypes.firstOrNull { it.llmName != null && answer.contains(it.llmName!!, ignoreCase = true) }
    }
}
