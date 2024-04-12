package com.example.diplom_bot

import com.example.diplom_bot.property.ChatBotProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(value = [ChatBotProperties::class])
class DiplomBotApplication
