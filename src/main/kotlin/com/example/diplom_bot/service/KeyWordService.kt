package com.example.diplom_bot.service

import com.example.diplom_bot.entity.KeyWord
import com.example.diplom_bot.repository.DisProblemRepository
import com.example.diplom_bot.repository.KeyWordRepository
import com.opencsv.CSVReaderBuilder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class KeyWordService(
    @Value("classpath:keywords.csv")
    private val resource: Resource,
    private val disProblemRepository: DisProblemRepository,
    private val keyWordRepository: KeyWordRepository
) {
    @Transactional
    fun loadKeyWords() {
        val disProblems = disProblemRepository.findAll()

        val csvReader = CSVReaderBuilder(resource.inputStream.reader()).build()

        val keyWords = csvReader.use { reader ->
            reader.readAll()
        }.filter { it.size == 3 && it.none { x -> x.isEmpty() } }.flatMap { line ->
            val disProblem = disProblems.find { it.id == line[1].toLong() }!!
            line[2].split(", ").map {
                it.substringBeforeLast(' ') to it.substringAfterLast(' ').toInt()
            }.map {
                KeyWord(it.first, it.second, disProblem)
            }
        }

        keyWordRepository.deleteAll()
        keyWordRepository.saveAll(keyWords)
    }
}
