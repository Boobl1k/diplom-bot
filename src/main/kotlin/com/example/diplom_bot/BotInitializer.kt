package com.example.diplom_bot

import Secrets
import com.example.diplom_bot.model.BotProblemUnit
import com.example.diplom_bot.model.GroupBotProblemUnit
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
    companion object : KLogging()

    private lateinit var allUnits: List<BotProblemUnit<*>>
    private lateinit var rootUnits: List<BotProblemUnit<*>>

    override fun run(args: ApplicationArguments?) {
        loadProblemUnits()
        val bot = createBot()
        bot.startPolling()
    }

    private fun loadProblemUnits() {
        rootUnits = problemGroupRepository.findRootGroups().map { GroupBotProblemUnit.fromRootProblemGroup(it) }
        allUnits =
            mutableListOf<BotProblemUnit<*>>().apply { rootUnits.forEach { it.addAllConnectedUnitsToList(this) } }
    }

    private fun createBot(): Bot {
        return bot {
            token = Secrets.BOT_TOKEN
            logLevel = LogLevel.Error
            dispatch {
                command("start") {
                    bot.sendGroups(ChatId.fromId(message.chat.id), rootUnits)
                }

                command("reload") {
                    loadProblemUnits()
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Бот был перезагружен")
                }

                callbackQuery {
                    logger.debug("callback {}: {}", callbackQuery.message?.chat?.username, callbackQuery.data)
                    val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery)
                    if (callbackQuery.data.startsWith(BotProblemUnit.CHOOSE_CALLBACK_PREFIX)) {
                        val group = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                        if (group.children.isNotEmpty()) {
                            bot.sendGroups(chatId, group.children, group, callbackQuery.message?.messageId)
                        } else {
                            // TODO
                        }
                        return@callbackQuery
                    }
                    if (callbackQuery.data.startsWith(BotProblemUnit.GO_BACK_CALLBACK_PREFIX)) {
                        val group = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                        bot.sendGroups(
                            chatId,
                            group.parent?.children ?: rootUnits,
                            group.parent,
                            callbackQuery.message?.messageId
                        )
                    }
                }

                text {
                    logger.debug("{}: {}", this.message.chat.username, this.text)
                }
            }
        }
    }

    private fun Bot.sendGroups(
        chatId: ChatId,
        groups: List<BotProblemUnit<*>>,
        root: BotProblemUnit<*>? = null,
        messageId: Long? = null
    ) {
        if (messageId != null) {
            editMessageText(
                chatId = chatId,
                messageId = messageId,
                text = "Выберите категорию проблемы:",
                replyMarkup = groupsToInlineKeyboard(groups, root)
            )
        } else {
            sendMessage(
                chatId = chatId,
                text = "Выберите категорию проблемы:",
                replyMarkup = groupsToInlineKeyboard(groups, root)
            )
        }
    }

    private fun groupsToInlineKeyboard(
        groups: List<BotProblemUnit<*>>,
        root: BotProblemUnit<*>?
    ): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.create(
            buttons = groups.map {
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = it.name,
                        callbackData = it.chooseCallbackData
                    )
                )
            } + (root?.let {
                listOf(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Назад",
                            callbackData = it.goBackCallbackData
                        )
                    )
                )
            } ?: listOf())
        )
    }
}
