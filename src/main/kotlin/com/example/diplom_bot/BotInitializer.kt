package com.example.diplom_bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class BotInitializer  : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val bot = bot {
            token = "7095057448:AAFZnMnJumKXQSCsz0fhfv8vSJt3Eg6lBDs"
            dispatch {
                command("start") {
                    bot.sendMessage(ChatId.fromId(message.chat.id), "кутакбаш")
                }
                text {
                    println("${this.message.chat.username}: ${this.text}")
                    bot.sendMessage(ChatId.fromId(message.chat.id), text = text)
                }
            }
        }
        bot.startPolling()
    }
}
