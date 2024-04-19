package com.example.diplom_bot

import com.example.diplom_bot.enum.UserAction
import com.example.diplom_bot.model.BotProblemUnit
import com.example.diplom_bot.model.Button
import com.example.diplom_bot.model.User
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.ProblemGroupRepository
import com.example.diplom_bot.service.DisProblemService
import com.example.diplom_bot.service.KeyWordService
import com.example.diplom_bot.service.UserContainer
import com.example.diplom_bot.service.UserProblemService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.contact
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
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
    private val userContainer: UserContainer,
    private val userProblemService: UserProblemService
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

                    val user = userContainer.find(chatId.id)

                    val action = UserAction.getActionFromCallbackQueryData(callbackQuery.data)

                    when (action) {
                        UserAction.CHOOSE -> {
                            val unit = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                            if (unit is BotProblemUnit.DisProblemBotProblemUnit) {
                                user.getCurrentProblem().disProblem = unit.entity
                            }
                            if (unit is BotProblemUnit.ProblemBotProblemUnit) {
                                user.getCurrentProblem().disProblem = unit.entity.disProblem
                                user.getCurrentProblem().problemCase = unit.entity
                            }
                            editMessage(unit.headerText, unit.buttons)
                        }

                        UserAction.GO_BACK -> {
                            val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                            if (unit is BotProblemUnit.GroupBotProblemUnit) {
                                user.clearCurrentProblem()
                            }
                            editMessage(unit.parent!!.headerText, unit.parent.buttons)
                        }

                        UserAction.GO_WRITE_DESCRIPTION -> {
                            editMessage(
                                "Напишите краткое описание проблемы, я постараюсь определить Вашу проблему",
                                listOf(Button(CallbackData.GO_START, "Назад"))
                            )
                        }

                        UserAction.GO_CHOOSE -> {
                            editMessage(rootUnit.headerText, rootUnit.buttons)
                        }

                        UserAction.GO_START -> {
                            bot.sendStartMessage(chatId)
                        }

                        UserAction.GO_SEND_TICKET -> {
                            sendMessage(
                                "Если вы хотите создать заявку самостоятельно, я могу предоставить вам инструкции. Или я могу сделать это за вас. Какой вариант вас интересует?",
                                listOf(
                                    Button(CallbackData.BOT_SEND_TICKET, "Пусть бот создаст"),
                                    Button(CallbackData.SEND_TICKET_BY_MYSELF, "Сам создам")
                                )
                            )
                        }

                        UserAction.BOT_SEND_TICKET -> {
                            bot.handleBotSendingTicket(user)
                        }

                        UserAction.SEND_TICKET_BY_MYSELF -> {
                            val disProblem = user.getCurrentProblem().disProblem!!
                            sendMessage(
                                text = """Для того, чтобы создать заявку: 
                                |1. Войдите в личный кабинет в https://kpfu.ru/
                                |2. Выберите раздел "ЗАЯВКИ НА IT-УСЛУГИ"
                                |3. Выберите "НОВАЯ ЗАЯВКА НА ОБСЛУЖИВАНИЕ IT-ИНФРАСТРУКТУРЫ"
                                |4. Тип заявки выберите ${disProblem.problemGroup.name} -> ${disProblem.name}
                                |5. Подробно опишите проблему в поле "Текст заявки"
                                |6. Заполните недостающие контактные данные
                                |7. Нажмите "Отправить заявку"
                            """.trimMargin(),
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }

                        UserAction.SEND_TICKET_SURE -> {
                            userProblemService.sendProblemToSupport(user)
                            sendMessage(
                                "Ваша заявка отправлена. Ожидайте звонка",
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }
                    }

                    if (action.userStateAfterAction != null) {
                        user.state = action.userStateAfterAction!!
                    }
                }

                text {
                    if (text.startsWith('/')) return@text

                    val chatId = ChatId.fromId(message.chat.id)
                    val alias = message.chat.username
                    logger.debug("{} {}: {}", chatId.id, alias, this.text)

                    val user = userContainer.find(chatId.id)

                    when (user.state) {
                        User.State.WRITING_DESCRIPTION -> {
                            user.createNewProblem().apply {
                                shortDescription = text
                            }

                            val footerButtons = listOf(
                                Button(CallbackData.GO_DESCRIPTION, "Попробовать еще раз"),
                                Button(CallbackData.GO_CHOOSE, "Выбрать категорию"),
                                Button(CallbackData.GO_START, "В начало")
                            )

                            user.state = User.State.OTHER
                            val problems = disProblemService.findByDescription(text)
                            if (problems.isEmpty()) {
                                sendMessage(
                                    if (text.length < 15) "Ваше описание слишком короткое, попробуйте еще раз"
                                    else "Я не могу определить вашу проблему. Возможно в Вашем описании есть опечатки. Попробуйте еще раз",
                                    footerButtons
                                )
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

                        User.State.SENDING_NAME -> {
                            user.name = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_PHONE -> {
                            user.phone = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_ADDRESS -> {
                            user.address = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_OFFICE_NUMBER -> {
                            user.officeNumber = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_PROBLEM_DETAILS -> {
                            user.getCurrentProblem().details = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.OTHER -> {}
                    }
                }

                contact {
                    val user = userContainer.find(chatId = message.chat.id)
                    user.phone = contact.phoneNumber
                    bot.sendMessage(
                        chatId = ChatId.fromId(user.chatId),
                        text = "Контакт получен",
                        replyMarkup = ReplyKeyboardRemove()
                    )
                    bot.handleBotSendingTicket(user)
                }
            }
        }
    }

    private fun Bot.handleBotSendingTicket(user: User) {
        val chatId = ChatId.fromId(user.chatId)
        if (user.name == null) {
            user.state = User.State.SENDING_NAME
            sendMessage(
                chatId = chatId,
                text = "Напишите пожалуйста свои ФИО"
            )
            return
        }
        if (user.phone == null) {
            user.state = User.State.SENDING_PHONE
            sendMessage(
                chatId = chatId,
                text = "Пожалуйста поделитесь контактом или напишите контактный номер телефона",
                replyMarkup = KeyboardReplyMarkup(
                    KeyboardButton(
                        "Отправить контакт",
                        requestContact = true
                    ),
                    resizeKeyboard = true
                )
            )
            return
        }
        if (user.address == null) {
            user.state = User.State.SENDING_ADDRESS
            sendMessage(
                chatId = chatId,
                text = "Напишите пожалуйста рабочий адрес"
            )
            return
        }
        if (user.officeNumber == null) {
            user.state = User.State.SENDING_OFFICE_NUMBER
            sendMessage(
                chatId = chatId,
                text = "Напишите пожалуйста номер рабочего кабинета"
            )
            return
        }
        if (user.getCurrentProblem().details == null) {
            user.state = User.State.SENDING_PROBLEM_DETAILS
            sendMessage(
                chatId = chatId,
                text = "Опишите пожалуйста вашу проблему как можно подробнее"
            )
            return
        }

        sendMessage(
            chatId = chatId,
            text = "Мы готовы отправить вашу заявку. Нажмите кнопку, чтобы отправить",
            replyMarkup = listOf(
                Button(CallbackData.SEND_TICKET_SURE, "Отправить"),
                Button(CallbackData.GO_START, "В начало")
            ).toInlineKeyboardMarkup()
        )
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

    private fun CallbackQueryHandlerEnvironment.sendMessage(text: String, buttons: List<Button>? = null) {
        val chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return)
        bot.sendMessage(
            chatId = chatId,
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
