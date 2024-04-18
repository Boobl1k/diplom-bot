package com.example.diplom_bot.service

import com.example.diplom_bot.model.User
import com.example.diplom_bot.model.UserProblem
import com.example.diplom_bot.property.ChatBotProperties
import org.springframework.stereotype.Service

@Service
class UserProblemService(
    private val mailService: MailService,
    private val chatBotProperties: ChatBotProperties
) {
    fun sendProblemToSupport(userProblem: UserProblem, user: User) {
        val text = """Сообщение отправлено чат-ботом
            |
            |Здравствуйте. Поступила заявка от пользователя на решение проблемы
            |
            |----------
            |
            |Данные заявителя:
            |
            |ФИО - ${user.name}
            |Телефон - ${user.phone}
            |Адрес - ${user.address}
            |Номер кабинета - ${user.officeNumber}
            |
            |----------
            |
            |Краткое описание проблемы:
            |
            |${userProblem.shortDescription ?: "Нет краткого описания"}
            |
            |----------
            |
            |Детали проблемы:
            |
            |${userProblem.details}
            |
            |----------
            |
        """.trimMargin()

        mailService.sendEmail(chatBotProperties.supportEmail, "Чат-бот. Проблема пользователя ${user.name}", text)
    }
}
