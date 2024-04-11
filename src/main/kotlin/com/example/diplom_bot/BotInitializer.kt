package com.example.diplom_bot

import Secrets
import com.example.diplom_bot.model.BotProblemUnit
import com.example.diplom_bot.repository.ProblemGroupRepository
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import mu.KLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class BotInitializer(
    private val problemGroupRepository: ProblemGroupRepository
) : ApplicationRunner {

    private enum class UserAction {
        CHOOSE,
        GO_BACK,
        SEND_TICKET;

        companion object {
            fun getActionFromCallbackQueryData(callbackQueryData: String): UserAction {
                return if (callbackQueryData.startsWith(BotProblemUnit.CHOOSE_CALLBACK_PREFIX)) CHOOSE
                else if (callbackQueryData.startsWith(BotProblemUnit.GO_BACK_CALLBACK_PREFIX)) GO_BACK
                else SEND_TICKET
            }
        }
    }

    companion object : KLogging()

    private lateinit var allUnits: List<BotProblemUnit<*>>
    private lateinit var rootUnit: BotProblemUnit<*>

    override fun run(args: ApplicationArguments?) {
        loadProblemUnits()
        val bot = createBot()
        bot.startPolling()
    }

    private fun loadProblemUnits() {
        rootUnit = BotProblemUnit.createRootBotProblemUnit(problemGroupRepository.findRootGroup())
        allUnits = rootUnit.getAllConnectedUnits()
    }

    private fun createBot(): Bot {
        return bot {
            token = Secrets.BOT_TOKEN
            logLevel = LogLevel.Error
            dispatch {
                command("start") {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = rootUnit.headerText,
                        replyMarkup = rootUnit.buttons.toInlineKeyboardMarkup()
                    )
                }

                command("reload") {
                    loadProblemUnits()
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Бот был перезагружен")
                }

                callbackQuery {
                    logger.debug("callback {}: {}", callbackQuery.message?.chat?.username, callbackQuery.data)
                    val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery)
                    val messageId = callbackQuery.message!!.messageId

                    val action = UserAction.getActionFromCallbackQueryData(callbackQuery.data)

                    when (action) {
                        UserAction.CHOOSE -> {
                            val unit = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                            bot.editMessageText(
                                chatId = chatId,
                                messageId = messageId,
                                text = unit.headerText,
                                replyMarkup = unit.buttons.toInlineKeyboardMarkup()
                            )
                        }
                        UserAction.GO_BACK -> {
                            val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                            bot.editMessageText(
                                chatId = chatId,
                                messageId = messageId,
                                text = unit.parent!!.headerText,
                                replyMarkup = unit.parent.buttons.toInlineKeyboardMarkup()
                            )
                        }
                        UserAction.SEND_TICKET -> {} // TODO
                    }
                }

                text {
                    logger.debug("{}: {}", this.message.chat.username, this.text)
                }
            }
        }
    }

    private fun List<BotProblemUnit.Button>.toInlineKeyboardMarkup(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.create(this.map {
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = it.name,
                    callbackData = it.callBackData
                )
            )
        })
    }
}
