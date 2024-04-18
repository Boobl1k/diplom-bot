package com.example.diplom_bot.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bot-properties")
data class ChatBotProperties(
    val botToken: String,
    val kfuAccessKey: String,
    val mailSenderEmail: String,
    val supportEmail: String
)
