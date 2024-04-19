package com.example.diplom_bot.service

import com.example.diplom_bot.entity.User
import com.example.diplom_bot.entity.UserProblem
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.UserProblemRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserProblemService(
    private val mailService: MailService,
    private val chatBotProperties: ChatBotProperties,
    private val userProblemRepository: UserProblemRepository
) {
    @Transactional
    fun sendProblemToSupport(user: User) {
        val text = """
            Сообщение отправлено чат-ботом
            
            Здравствуйте. Поступила заявка от пользователя на решение проблемы
            
            ----------
            
            Данные заявителя:
            
            ФИО - ${user.name}
            Телефон - ${user.phone}
            Адрес - ${user.address}
            Номер кабинета - ${user.officeNumber}
            
            ----------
            
            Вид проблемы:
            
            ${user.currentProblem!!.disProblem!!.problemGroup.name} -> 
            ${user.currentProblem!!.disProblem!!.name}
            
            Случай: 
            
            ${user.currentProblem!!.problemCase?.condition ?: "Неопределенный"} 
            
            ----------
            
            Краткое описание проблемы:
            
            ${user.currentProblem!!.shortDescription ?: "Нет краткого описания"}
            
            ----------
            
            Детали проблемы:
            
            ${user.currentProblem!!.details!!}
            
            ----------
            
        """.trimIndent()

        mailService.sendEmail(chatBotProperties.supportEmail, "Чат-бот. Проблема пользователя ${user.name}", text)
        markProblemSent(user)
    }

    @Transactional
    fun createNewProblem(user: User): UserProblem {
        clearCurrentProblem(user)
        val problem = UserProblem(user)
        user.currentProblem = userProblemRepository.save(problem)
        return problem
    }

    @Transactional
    fun getCurrentProblem(user: User): UserProblem {
        return user.currentProblem ?: createNewProblem(user)
    }

    @Transactional
    fun clearCurrentProblem(user: User) {
        val problem = user.currentProblem
        if (problem != null) {
            user.currentProblem = null
            if (!problem.sent) {
                userProblemRepository.delete(problem)
            }
        }
    }

    @Transactional
    fun markProblemSent(user: User) {
        val problem = user.currentProblem!!
        problem.sent = true
        clearCurrentProblem(user)
        user.sentProblems.add(problem)
    }
}
