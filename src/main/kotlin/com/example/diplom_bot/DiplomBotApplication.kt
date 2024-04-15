package com.example.diplom_bot

import com.example.diplom_bot.property.ChatBotProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(value = [ChatBotProperties::class])
@EnableFeignClients
@EnableScheduling
class DiplomBotApplication
