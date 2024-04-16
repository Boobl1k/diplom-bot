package com.example.diplom_bot.service

import com.example.diplom_bot.client.KfuClientAdapter
import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.repository.DisProblemRepository
import jakarta.transaction.Transactional
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DisProblemService(
    private val kfuClientAdapter: KfuClientAdapter,
    private val disProblemRepository: DisProblemRepository
) {
    companion object : KLogging()

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

        logger.info { "DIS problems updated" }
    }

    fun findByDescription(description: String): List<DisProblem> {
        val problems = disProblemRepository.findAll()

        logger.debug("description: {}", description)
        val formattedDescription = formatDescription(description)
        logger.debug("formatted: {}", formattedDescription)

        val problemsWithScores = problems.map {
            it.keyWords.sumOf { keyword ->
                val regex = Regex(Regex.escape(keyword.keyWord), RegexOption.IGNORE_CASE)
                regex.findAll(formattedDescription).count().toLong() * keyword.weight
            } to it
        }
        problemsWithScores.sortedByDescending { it.first }.take(10).forEach {
            logger.debug("{} {}", it.first, it.second.name)
        }

        return problemsWithScores
            .filter { it.first > 5 }
            .sortedByDescending { it.first }
            .take(5)
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
