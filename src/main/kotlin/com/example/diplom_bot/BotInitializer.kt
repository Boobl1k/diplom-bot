package com.example.diplom_bot

import com.example.diplom_bot.model.BotProblemUnit
import com.example.diplom_bot.model.Button
import com.example.diplom_bot.model.User
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.ProblemGroupRepository
import com.example.diplom_bot.service.DisProblemService
import com.example.diplom_bot.service.KeyWordService
import com.example.diplom_bot.service.UserContainer
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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BotInitializer(
    private val problemGroupRepository: ProblemGroupRepository,
    private val chatBotProperties: ChatBotProperties,
    private val keyWordService: KeyWordService,
    private val disProblemService: DisProblemService,
    private val userContainer: UserContainer
) : ApplicationRunner {

    private enum class UserAction {
        GO_WRITE_DESCRIPTION {
            override val callBackPrefix: String
                get() = "go_description"
        },
        GO_CHOOSE {
            override val callBackPrefix: String
                get() = "go_choose"
        },
        GO_START {
            override val callBackPrefix: String
                get() = GO_START_CALLBACK_DATA
        },
        CHOOSE {
            override val callBackPrefix: String
                get() = CHOOSE_CALLBACK_PREFIX
        },
        GO_BACK {
            override val callBackPrefix: String
                get() = GO_BACK_CALLBACK_PREFIX
        };

        abstract val callBackPrefix: String

        companion object {
            fun getActionFromCallbackQueryData(callbackQueryData: String): UserAction {
                return entries.find { callbackQueryData.startsWith(it.callBackPrefix) }!!
            }
        }
    }

    companion object : KLogging() {
        private val GO_BACK_DESCRIPTION_CALLBACK = GO_BACK_CALLBACK_PREFIX + "_description"
    }

    private lateinit var allUnits: List<BotProblemUnit<*>>
    private lateinit var rootUnit: BotProblemUnit<*>

    override fun run(args: ApplicationArguments?) {
        keyWordService.loadKeyWords()

        loadProblemUnits()
        val bot = createBot()
        bot.startPolling()
    }

    @Scheduled(cron = "0 0 0/1 * * ?")
    private fun loadProblemUnits() {
        rootUnit = BotProblemUnit.createRootBotProblemUnit(problemGroupRepository.findRootGroup())
        allUnits = rootUnit.getAllConnectedUnits()

        logger.info { "bot updated" }
    }

    private fun createBot(): Bot {
        return bot {
            token = chatBotProperties.botToken
            logLevel = LogLevel.Error
            dispatch {
                command("start") {
                    bot.sendStartMessage(ChatId.fromId(message.chat.id))
                }

                command("reload") {
                    loadProblemUnits()
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Бот был перезагружен")
                }

                callbackQuery {
                    logger.debug("callback {}: {}", callbackQuery.message?.chat?.username, callbackQuery.data)
                    val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery)
                    val alias = callbackQuery.message!!.chat.username

                    val action = UserAction.getActionFromCallbackQueryData(callbackQuery.data)

                    when (action) {
                        UserAction.CHOOSE -> {
                            val unit = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                            bot.sendMessage(
                                chatId = chatId,
                                text = unit.headerText,
                                replyMarkup = unit.buttons.toInlineKeyboardMarkup()
                            )
                        }

                        UserAction.GO_BACK -> {
                            if (callbackQuery.data == GO_BACK_DESCRIPTION_CALLBACK) {
                                bot.sendDescriptionWritingMessage(chatId)
                            } else {
                                val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                                bot.sendMessage(
                                    chatId = chatId,
                                    text = unit.parent!!.headerText,
                                    replyMarkup = unit.parent.buttons.toInlineKeyboardMarkup()
                                )
                            }
                        }

                        UserAction.GO_WRITE_DESCRIPTION -> {
                            bot.sendDescriptionWritingMessage(chatId)
                        }

                        UserAction.GO_CHOOSE -> {
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                            bot.sendMessage(
                                chatId = chatId,
                                text = rootUnit.headerText,
                                replyMarkup = rootUnit.buttons.toInlineKeyboardMarkup()
                            )
                        }

                        UserAction.GO_START -> {
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                            bot.sendStartMessage(chatId)
                        }
                    }
                }

                text {
                    val chatId = ChatId.fromId(message.chat.id)
                    val alias = message.chat.username
                    logger.debug("{} {}: {}", chatId.id, alias, this.text)

                    if (userContainer.isUserWritingDescription(chatId.id)) {
                        val problems = disProblemService.findByDescription(text)
                        val buttons = problems.map { Button(it.chooseCallbackData, it.name) } +
                                listOf(Button(GO_BACK_DESCRIPTION_CALLBACK, "Назад"))
                        bot.sendMessage(
                            chatId = chatId,
                            text = "Выберите проблему",
                            replyMarkup = buttons.toInlineKeyboardMarkup()
                        )
                    }
                }
            }
        }
    }

    private fun Bot.sendStartMessage(chatId: ChatId.Id) {
        sendMessage(
            chatId = chatId,
            text = """Я предлагаю Вам 2 варианта определения проблемы:
                |1. Вы вводите описание проблемы, затем я предложу вам несколько видов проблем, Вы выберите свою
                |2. Я вам предлагаю категории, Вы выбираете нужную
            """.trimMargin(),
            replyMarkup = listOf(
                Button(UserAction.GO_WRITE_DESCRIPTION.callBackPrefix, "Хочу написать описание"),
                Button(UserAction.GO_CHOOSE.callBackPrefix, "Хочу выбирать категории")
            ).toInlineKeyboardMarkup()
        )
    }

    private fun Bot.sendDescriptionWritingMessage(chatId: ChatId.Id) {
        userContainer.setUserState(chatId.id, User.State.WRITING_DESCRIPTION)
        sendMessage(
            chatId = chatId,
            text = "Напишите краткое описание проблемы, я постараюсь определить Вашу проблему"
        )
    }

    private fun List<Button>.toInlineKeyboardMarkup(): InlineKeyboardMarkup {
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
