package com.example.diplom_bot.service

import com.example.diplom_bot.client.KfuClientAdapter
import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.repository.DisProblemRepository
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DisProblemService(
    private val kfuClientAdapter: KfuClientAdapter,
    private val disProblemRepository: DisProblemRepository
) {
    companion object : KLogging()

    private lateinit var problems: List<DisProblem>

    @Scheduled(cron = "0 0 0/1 * * ?")
    @Transactional
    fun updateDisProblems() {
        val externalProblemTypesList = kfuClientAdapter.getRequestTypeList()
        val entities = disProblemRepository.findAll()

        entities.forEach { entity ->
            val external = externalProblemTypesList.find { it.id == entity.externalDisProblemId }
            entity.enabled = external != null
            if (external != null) {
                entity.name = external.name
            }
        }

        externalProblemTypesList.forEach { external ->
            if (entities.none { it.externalDisProblemId == external.id }) {
                logger.warn("Problem type \"{}\" not found, external id = {}", external.name, external.id)
            }
        }

        problems = entities

        logger.info { "DIS problems updated" }
    }

    @Transactional(readOnly = true)
    fun findByDescription(description: String): List<DisProblem> {
        if (!this::problems.isInitialized) {
            problems = disProblemRepository.findAll()
        }

        logger.debug("description: {}", description)
        val formattedDescription = formatDescription(description)
        logger.debug("formatted: {}", formattedDescription)

        val problemsWithScores = problems.map {
            it.keyWords.sumOf { keyword ->
                val regex = Regex(Regex.escape(keyword.keyWord), RegexOption.IGNORE_CASE)
                regex.findAll(formattedDescription).count().toLong() * keyword.weight
            } to it
        }

        return problemsWithScores
            .filter { it.first > 5 }
            .sortedByDescending { it.first }
            .take(5)
            .onEach { logger.debug("{} {}", it.first, it.second.name) }
            .map { it.second }
    }

    private fun formatDescription(description: String): String {
        // удаление всех знаков препинания кроме точек
        val punctuationRegex = Regex("[\",\\-:()\\[\\]{}]")
        var processedText = description.replace(punctuationRegex, " ")

        // Удаление всех непрерывных последовательностей пробелов
        processedText = processedText.replace("\\s+".toRegex(), " ")

        return processedText
    }
}
