package com.example.diplom_bot.service

import com.example.diplom_bot.entity.User
import com.example.diplom_bot.entity.UserProblem
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.UserProblemRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.io.File

@Service
class UserProblemService(
    private val mailService: MailService,
    private val chatBotProperties: ChatBotProperties,
    private val userProblemRepository: UserProblemRepository
) {
    @Transactional
    fun sendProblemToSupport(user: User, screenshot: File? = null) {
        val userProblem = user.currentProblem!!
        var text = """
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
            
            ${userProblem.disProblem!!.problemGroup.name} -> 
            ${userProblem.disProblem!!.name}
            
            ----------
        """.trimIndent()

        if (userProblem.iasModule != null) {
            text += """
                
                Модуль:
                
                ${userProblem.iasModule!!.name}
                
                ----------
            """.trimIndent()

            if (userProblem.iasService != null) {
                text += """
                    
                    Сервис:
                    
                    ${userProblem.iasService!!.name}
                    
                    ----------
                """.trimIndent()
            }
        }

        if (userProblem.problemCase != null) {
            text += """
                
                Случай: 
                
                ${userProblem.problemCase?.condition ?: "Неопределенный"} 
                
                ----------
            """.trimIndent()
        }

        text += """
            
            Краткое описание проблемы:
            
            ${userProblem.shortDescription ?: "Нет краткого описания"}
            
            ----------
            
            Детали проблемы:
            
            ${userProblem.details!!}
            
            ----------
            
        """.trimIndent()

        if (screenshot != null) {
            mailService.sendEmail(
                address = chatBotProperties.supportEmail,
                subject = "Чат-бот. Проблема пользователя ${user.name}",
                message = text,
                fileName = userProblem.fileName!!,
                file = screenshot,
            )
        } else mailService.sendEmail(
            address = chatBotProperties.supportEmail,
            subject = "Чат-бот. Проблема пользователя ${user.name}",
            message = text
        )

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
