package com.example.diplom_bot.service

import com.example.diplom_bot.client.KfuClientAdapter
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
    companion object: KLogging()

    @Scheduled(cron = "0 0 0/1 * * ?")
    @Transactional
    fun updateDisProblems() {
        val externalProblemTypesList = kfuClientAdapter.getRequestTypeList()
        val entities = disProblemRepository.findAll()

        entities.forEach { entity ->
            val external = externalProblemTypesList.find { it.id == entity.externalDisProblemId }
            entity.enabled = external != null
            if(external != null) {
                entity.name = external.name
            }
        }

        externalProblemTypesList.forEach { external ->
            if(entities.none { it.externalDisProblemId == external.id }) {
                logger.warn("Problem type \"{}\" not found, external id = {}", external.name, external.id)
            }
        }

        logger.info { "DIS problems updated" }
    }
}
