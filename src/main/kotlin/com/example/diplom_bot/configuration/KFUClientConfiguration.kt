package com.example.diplom_bot.configuration

import com.example.diplom_bot.property.ChatBotProperties
import feign.RequestInterceptor
import org.springframework.context.annotation.Bean

class KFUClientConfiguration(
    private val chatBotProperties: ChatBotProperties
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor = RequestInterceptor {
        it.query("p_access_key", chatBotProperties.kfuAccessKey)
    }
}
