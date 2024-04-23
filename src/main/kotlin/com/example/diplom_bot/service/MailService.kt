package com.example.diplom_bot.service

import com.example.diplom_bot.property.ChatBotProperties
import mu.KLogging
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.io.File

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val chatBotProperties: ChatBotProperties
) {

    companion object : KLogging()

    fun sendEmail(
        address: String,
        subject: String,
        message: String
    ) {
        sendEmailPrivate(address, subject, message)
    }

    fun sendEmail(
        address: String,
        subject: String,
        message: String,
        fileName: String,
        file: File
    ) {
        sendEmailPrivate(address, subject, message, fileName, file)
    }

    private fun sendEmailPrivate(
        address: String,
        subject: String,
        message: String,
        fileName: String? = null,
        file: File? = null
    ) {
        val mimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)
        helper.setSubject(subject)
        helper.setText(message)
        helper.setFrom(chatBotProperties.mailSenderEmail)
        helper.setTo(address)
        file?.let { helper.addAttachment(fileName!!, file) }
        javaMailSender.send(mimeMessage)
    }
}
