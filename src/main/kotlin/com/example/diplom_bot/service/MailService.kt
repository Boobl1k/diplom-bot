package com.example.diplom_bot.service

import com.example.diplom_bot.property.ChatBotProperties
import mu.KLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val chatBotProperties: ChatBotProperties
) {

    companion object : KLogging()

    fun sendEmail(address: String, subject: String, message: String) {
        javaMailSender.send(SimpleMailMessage().apply {
            setTo(address)
            this.subject = subject
            text = message
            from = chatBotProperties.mailSenderEmail
        })
    }
}
