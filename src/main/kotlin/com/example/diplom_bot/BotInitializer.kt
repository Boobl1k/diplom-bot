package com.example.diplom_bot

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.enum.UserAction
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
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
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
    companion object : KLogging()

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
                    logger.debug(
                        "callback {} {}: {}",
                        callbackQuery.message?.chat?.id,
                        callbackQuery.message?.chat?.username,
                        callbackQuery.data
                    )

                    val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery)

                    val action = UserAction.getActionFromCallbackQueryData(callbackQuery.data)

                    when (action) {
                        UserAction.CHOOSE -> {
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                            val unit = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                            editMessage(unit.headerText, unit.buttons)
                        }

                        UserAction.GO_BACK -> {
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                            val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                            editMessage(unit.parent!!.headerText, unit.parent.buttons)
                        }

                        UserAction.GO_WRITE_DESCRIPTION -> {
                            userContainer.setUserState(chatId.id, User.State.WRITING_DESCRIPTION)
                            editMessage(
                                "Напишите краткое описание проблемы, я постараюсь определить Вашу проблему",
                                listOf(Button(CallbackData.GO_START, "Назад"))
                            )
                        }

                        UserAction.GO_CHOOSE -> {
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                            editMessage(rootUnit.headerText, rootUnit.buttons)
                        }

                        UserAction.GO_START -> {
                            bot.sendStartMessage(chatId)
                        }

                        UserAction.SEND_TICKET -> {
                            val unit = allUnits.find {
                                it is BotProblemUnit.DisProblemBotProblemUnit &&
                                        it.sendTicketCallbackData == callbackQuery.data
                            }!!
                            val disProblem = unit.entity as DisProblem
                            bot.sendMessage(
                                chatId = chatId,
                                text = """Для того, чтобы создать заявку: 
                                |1. Войдите в личный кабинет в https://kpfu.ru/
                                |2. Выберите раздел "ЗАЯВКИ НА IT-УСЛУГИ"
                                |3. Выберите "НОВАЯ ЗАЯВКА НА ОБСЛУЖИВАНИЕ IT-ИНФРАСТРУКТУРЫ"
                                |4. Тип заявки выберите ${disProblem.problemGroup.name} -> ${disProblem.name}
                                |5. Подробно опишите проблему в поле "Текст заявки"
                                |6. Заполните недостающие контактные данные
                                |7. Нажмите "Отправить заявку"
                            """.trimMargin(),
                                replyMarkup = listOf(Button(CallbackData.GO_START, "В начало")).toInlineKeyboardMarkup()
                            )
                        }
                    }
                }

                text {
                    val chatId = ChatId.fromId(message.chat.id)
                    val alias = message.chat.username
                    logger.debug("{} {}: {}", chatId.id, alias, this.text)

                    if (userContainer.isUserWritingDescription(chatId.id)) {
                        val footerButtons = listOf(
                            Button(CallbackData.GO_DESCRIPTION, "Попробовать еще раз"),
                            Button(CallbackData.GO_CHOOSE, "Выбрать категорию"),
                            Button(CallbackData.GO_START, "В начало")
                        )

                        userContainer.setUserState(chatId.id, User.State.OTHER)
                        val problems = disProblemService.findByDescription(text)
                        if (problems.isEmpty()) {
                            sendMessage(
                                if (text.length < 15) "Ваше описание слишком короткое, попробуйте еще раз"
                                else "Я не могу определить вашу проблему. Возможно в Вашем описании есть опечатки. Попробуйте еще раз",
                                footerButtons
                            )
                            userContainer.setUserState(chatId.id, User.State.OTHER)
                        } else {
                            val problemDescriptions = problems.mapIndexed { index, disProblem ->
                                "${index + 1}. ${disProblem.problemGroup.name} -> ${disProblem.name}\n"
                            }.joinToString(separator = "") { it }
                            sendMessage(
                                """Выберите проблему:
                                |$problemDescriptions
                                |Если вашей проблемы нет, Вы можете попробовать еще раз или найти проблему по категориям
                            """.trimMargin(),
                                problems.mapIndexed { index, disProblem ->
                                    Button(
                                        disProblem.chooseCallbackData,
                                        "${index + 1}. ${disProblem.name}"
                                    )
                                } + footerButtons
                            )
                        }
                    }
                }
            }
        }
    }

    private fun CallbackQueryHandlerEnvironment.editMessage(text: String, buttons: List<Button>? = null) {
        val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return)
        bot.editMessageText(
            chatId = chatId,
            messageId = callbackQuery.message!!.messageId,
            text = text,
            replyMarkup = buttons?.toInlineKeyboardMarkup()
        )
    }

    private fun TextHandlerEnvironment.sendMessage(text: String, buttons: List<Button>? = null) {
        val chatId = ChatId.fromId(message.chat.id)
        bot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = buttons?.toInlineKeyboardMarkup()
        )
    }

    private fun Bot.sendStartMessage(chatId: ChatId.Id) {
        userContainer.setUserState(chatId.id, User.State.OTHER)
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
