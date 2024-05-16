package com.example.diplom_bot

import com.example.diplom_bot.entity.IASProblem
import com.example.diplom_bot.entity.User
import com.example.diplom_bot.enum.UserAction
import com.example.diplom_bot.model.*
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.ProblemGroupRepository
import com.example.diplom_bot.service.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.ContactHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.files.Document
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import mu.KLogging
import org.apache.commons.io.FileUtils
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW
import org.springframework.transaction.support.TransactionTemplate
import java.io.File

@Component
class BotInitializer(
    private val problemGroupRepository: ProblemGroupRepository,
    private val chatBotProperties: ChatBotProperties,
    private val keyWordService: KeyWordService,
    private val disProblemService: DisProblemService,
    private val userService: UserService,
    private val userProblemService: UserProblemService,
    private val txTemplate: TransactionTemplate,
    private val llmChatService: LLMChatService
) : ApplicationRunner {
    companion object : KLogging()

    private lateinit var allUnits: List<BotProblemUnit<*>>
    private lateinit var rootUnit: BotProblemUnit<*>

    override fun run(args: ApplicationArguments?) {
        txTemplate.propagationBehavior = PROPAGATION_REQUIRES_NEW
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
                myCommand("start") {
                    bot.sendStartMessage(message.chat.id)
                }

                myCommand("solvemyproblem") {
                    bot.sendStartMessage(message.chat.id)
                }

                myCommand("updatemyphone") { user ->
                    llmChatService.clearHistory(user.chatId)
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "Ваш телефон - ${user.phone ?: ""}",
                        replyMarkup = listOf(
                            Button(CallbackData.UPDATE_PHONE, "Обновить номер телефона"),
                        ).toInlineKeyboardMarkup()
                    )
                }

                myCallbackQuery { user ->
                    logger.debug(
                        "callback {} {}: {}",
                        callbackQuery.message?.chat?.id,
                        callbackQuery.message?.chat?.username,
                        callbackQuery.data
                    )

                    val action = UserAction.getActionFromCallbackQueryData(callbackQuery.data)

                    when (action) {
                        UserAction.CHOOSE -> {
                            llmChatService.clearHistory(user.chatId)
                            val unit = allUnits.find { it.chooseCallbackData == callbackQuery.data }!!
                            if (unit is BotProblemUnit.DisProblemBotProblemUnit) {
                                userProblemService.getCurrentProblem(user).disProblem = unit.entity
                                userProblemService.getCurrentProblem(user).iasService = null
                                userProblemService.getCurrentProblem(user).iasModule = null
                            }
                            if (unit is BotProblemUnit.ProblemBotProblemUnit) {
                                userProblemService.getCurrentProblem(user).disProblem = unit.entity.disProblem
                                userProblemService.getCurrentProblem(user).problemCase = unit.entity
                                userProblemService.getCurrentProblem(user).iasService = null
                                userProblemService.getCurrentProblem(user).iasModule = null
                            }
                            if (unit is BotProblemUnit.IASProblemBotProblemUnit) {
                                userProblemService.getCurrentProblem(user).disProblem = unit.entity
                            }
                            if (unit is BotProblemUnit.IASModuleBotProblemUnit) {
                                userProblemService.getCurrentProblem(user).iasModule = unit.entity
                            }
                            if (unit is BotProblemUnit.IASServiceBotProblemUnit) {
                                userProblemService.getCurrentProblem(user).iasModule = unit.entity.iasModule
                                userProblemService.getCurrentProblem(user).iasService = unit.entity
                            }
                            editMessage(unit.headerText, unit.buttons)
                        }

                        UserAction.GO_BACK -> {
                            llmChatService.clearHistory(user.chatId)
                            val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                            if (unit is BotProblemUnit.GroupBotProblemUnit) {
                                userProblemService.clearCurrentProblem(user)
                            }
                            editMessage(unit.parent!!.headerText, unit.parent.buttons)
                        }

                        UserAction.GO_WRITE_DESCRIPTION -> {
                            llmChatService.clearHistory(user.chatId)
                            editMessage(
                                "Напишите краткое описание проблемы, я постараюсь определить Вашу проблему",
                                listOf(Button(CallbackData.GO_START, "Назад"))
                            )
                        }

                        UserAction.GO_CHOOSE -> {
                            llmChatService.clearHistory(user.chatId)
                            editMessage(rootUnit.headerText, rootUnit.buttons)
                        }

                        UserAction.GO_START -> {
                            llmChatService.clearHistory(user.chatId)
                            bot.sendStartMessage(callbackQuery.message?.chat?.id!!)
                        }

                        UserAction.GO_SEND_TICKET -> {
                            llmChatService.clearHistory(user.chatId)
                            sendMessage(
                                "Если вы хотите создать заявку самостоятельно, я могу предоставить вам инструкции. Или я могу сделать это за вас. Какой вариант вас интересует?",
                                listOf(
                                    Button(CallbackData.BOT_SEND_TICKET, "Пусть бот создаст"),
                                    Button(CallbackData.SEND_TICKET_BY_MYSELF, "Сам создам")
                                )
                            )
                        }

                        UserAction.BOT_SEND_TICKET -> {
                            llmChatService.clearHistory(user.chatId)
                            bot.handleBotSendingTicket(user)
                        }

                        UserAction.SEND_TICKET_BY_MYSELF -> {
                            llmChatService.clearHistory(user.chatId)
                            val disProblem = userProblemService.getCurrentProblem(user).disProblem!!
                            sendMessage(
                                text = """
                                    Для того, чтобы создать заявку: 
                                    1. Войдите в личный кабинет в https://kpfu.ru/
                                    2. Выберите раздел "ЗАЯВКИ НА IT-УСЛУГИ"
                                    3. Выберите "НОВАЯ ЗАЯВКА НА ОБСЛУЖИВАНИЕ IT-ИНФРАСТРУКТУРЫ"
                                    4. Тип заявки выберите ${disProblem.problemGroup.name} -> ${disProblem.name}
                                    5. Подробно опишите проблему в поле "Текст заявки"
                                    6. Заполните недостающие контактные данные
                                    7. Нажмите "Отправить заявку"
                                """.trimIndent(),
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }

                        UserAction.SEND_TICKET_SURE -> {
                            llmChatService.clearHistory(user.chatId)
                            val fileId = user.currentProblem!!.fileId
                            if (fileId != null) {
                                val file = File.createTempFile("user_screenshot", "")
                                FileUtils.writeByteArrayToFile(file, bot.downloadFileBytes(fileId)!!)
                                userProblemService.sendProblemToSupport(
                                    user,
                                    callbackQuery.message?.chat?.username,
                                    file
                                )
                            } else {
                                userProblemService.sendProblemToSupport(user, callbackQuery.message?.chat?.username)
                            }
                            sendMessage(
                                "Ваша заявка отправлена. Ожидайте звонка",
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }

                        UserAction.UPDATE_PHONE -> {
                            llmChatService.clearHistory(user.chatId)
                            bot.sendMessage(
                                chatId = ChatId.fromId(callbackQuery.message?.chat?.id!!),
                                text = "Пожалуйста поделитесь контактом или напишите контактный номер телефона",
                                replyMarkup = KeyboardReplyMarkup(
                                    KeyboardButton(
                                        "Отправить контакт",
                                        requestContact = true
                                    ),
                                    resizeKeyboard = true
                                )
                            )
                        }

                        UserAction.GO_TALK_WITH_LLM -> {
                            sendMessage(
                                "Напишите что у вас случилось. Я постараюсь помочь Вам",
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }

                        UserAction.CONTINUE_TALKING_WITH_LLM -> {
                            val llmResponse = llmChatService.sendNotMyProblem(user.chatId)
                            bot.handleLLMResponse(user.chatId, llmResponse)
                        }

                        UserAction.REGENERATE -> {
                            val llmResponse = llmChatService.regenerate(user.chatId)
                            bot.handleLLMResponse(user.chatId, llmResponse)
                        }
                    }

                    if (action.userStateAfterAction != null) {
                        user.state = action.userStateAfterAction!!
                    }
                }

                myText { user ->
                    logger.debug("{} {}: {}", message.chat.id, message.chat.username, this.text)

                    if (text.startsWith('/')) return@myText

                    when (user.state) {
                        User.State.WRITING_DESCRIPTION -> {
                            userProblemService.createNewProblem(user).apply {
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
                                    """
                                        |Выберите проблему:
                                        |$problemDescriptions
                                        |Если вашей проблемы нет, Вы можете попробовать еще раз или найти проблему по категориям
                                    """.trimMargin(),
                                    problems.mapIndexed { index, disProblem ->
                                        Button(
                                            if (disProblem is IASProblem) disProblem.chooseCallbackData
                                            else disProblem.chooseCallbackData,
                                            "${index + 1}. ${disProblem.name}"
                                        )
                                    } + footerButtons
                                )
                            }
                        }

                        User.State.SENDING_PHONE -> {
                            user.phone = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш номер",
                                replyMarkup = ReplyKeyboardRemove()
                            )
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_PROBLEM_DETAILS -> {
                            userProblemService.getCurrentProblem(user).details = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_SCREENSHOTS -> {}

                        User.State.UPDATING_PHONE -> {
                            user.state = User.State.OTHER
                            user.phone = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш номер",
                                replyMarkup = ReplyKeyboardRemove()
                            )
                        }

                        User.State.OTHER -> {
                            user.state = User.State.TALKING_WITH_LLM
                            bot.handleLLMResponse(user.chatId, llmChatService.sendMessage(user.chatId, text))
                        }

                        User.State.TALKING_WITH_LLM -> {
                            bot.handleLLMResponse(user.chatId, llmChatService.sendMessage(user.chatId, text))
                        }
                    }
                }

                myContact { user ->
                    user.phone = contact.phoneNumber
                    bot.sendMessage(
                        chatId = ChatId.fromId(user.chatId),
                        text = "Контакт получен",
                        replyMarkup = ReplyKeyboardRemove()
                    )
                    if (user.state == User.State.UPDATING_PHONE) {
                        bot.sendMessage(
                            chatId = ChatId.fromId(user.chatId),
                            text = "Я записал Ваш номер",
                            replyMarkup = ReplyKeyboardRemove()
                        )
                    } else {
                        bot.handleBotSendingTicket(user)
                    }
                }

                myPhotos { user ->
                    if (user.state == User.State.SENDING_SCREENSHOTS) {
                        val largestPhoto = media.maxBy { it.height }
                        userProblemService.getCurrentProblem(user).fileId = largestPhoto.fileId
                        userProblemService.getCurrentProblem(user).fileName = "screenshot.jpg"
                        bot.handleBotSendingTicket(user)
                    }
                }

                myDocument { user ->
                    if (user.state == User.State.SENDING_SCREENSHOTS) {
                        userProblemService.getCurrentProblem(user).fileId = media.fileId
                        userProblemService.getCurrentProblem(user).fileName = media.fileName
                        bot.handleBotSendingTicket(user)
                    }
                }
            }
        }
    }

    private fun Dispatcher.myCallbackQuery(callback: CallbackQueryHandlerEnvironment.(user: User) -> Unit) {
        callbackQuery {
            txTemplate.execute {
                val user = userService.find(callbackQuery.message!!.chat.id)
                callback(user)
            }
        }
    }

    private fun Dispatcher.myText(callback: TextHandlerEnvironment.(user: User) -> Unit) {
        text {
            txTemplate.execute {
                val user = userService.find(message.chat.id)
                callback(user)
            }
        }
    }

    private fun Dispatcher.myContact(callback: ContactHandlerEnvironment.(user: User) -> Unit) {
        contact {
            txTemplate.execute {
                val user = userService.find(message.chat.id)
                callback(user)
            }
        }
    }

    private fun Dispatcher.myCommand(command: String, callback: CommandHandlerEnvironment.(user: User) -> Unit) {
        command(command) {
            txTemplate.execute {
                val user = userService.find(message.chat.id)
                callback(user)
            }
        }
    }

    private fun Dispatcher.myPhotos(callback: MediaHandlerEnvironment<List<PhotoSize>>.(user: User) -> Unit) {
        photos {
            txTemplate.execute {
                val user = userService.find(message.chat.id)
                callback(user)
            }
        }
    }

    private fun Dispatcher.myDocument(callback: MediaHandlerEnvironment<Document>.(user: User) -> Unit) {
        document {
            txTemplate.execute {
                val user = userService.find(message.chat.id)
                callback(user)
            }
        }
    }

    private fun Bot.handleBotSendingTicket(user: User) {
        llmChatService.clearHistory(user.chatId)
        val chatId = ChatId.fromId(user.chatId)
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

        if (userProblemService.getCurrentProblem(user).iasModule != null) {
            val userProblem = userProblemService.getCurrentProblem(user)
            val iasModule = userProblem.iasModule!!
            val iasService = userProblem.iasService ?: iasModule.services[0]

            if (userProblem.details == null) {
                val infoList = mutableListOf<String>()
                if (iasService.groupNeeded) infoList.add("Номер группы")
                if (iasService.applicantName != null) infoList.add(iasService.applicantName)
                if (iasService.additionalInfo != null) infoList.add(iasService.additionalInfo)

                user.state = User.State.SENDING_PROBLEM_DETAILS
                sendMessage(
                    chatId = chatId,
                    text = "Напишите пожалуйста следующую информацию одним сообщением: ${infoList.joinToString(", ")}"
                )

                return
            }
            if (userProblem.fileId == null) {
                user.state = User.State.SENDING_SCREENSHOTS
                sendMessage(
                    chatId = chatId,
                    text = "Отправьте пожалуйста скриншот"
                )

                return
            }
        } else if (userProblemService.getCurrentProblem(user).details == null) {
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

    private fun Bot.sendStartMessage(chatId: Long) {
        llmChatService.clearHistory(chatId)
        sendMessage(
            chatId = chatId,
            text = """
                Я предлагаю Вам 2 варианта определения проблемы:
                1. Вы вводите описание проблемы, затем я предложу вам несколько видов проблем, Вы выберите свою
                2. Я вам предлагаю категории, Вы выбираете нужную
            """.trimIndent(),
            buttons = listOf(
                Button(UserAction.GO_WRITE_DESCRIPTION.callBackPrefix, "Хочу написать описание"),
                Button(UserAction.GO_TALK_WITH_LLM.callBackPrefix, "Хочу пообщаться"),
                Button(UserAction.GO_CHOOSE.callBackPrefix, "Хочу выбирать категории")
            )
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

    private fun Bot.sendMessage(chatId: Long, text: String, buttons: List<Button>? = null) {
        sendMessage(
            chatId = ChatId.fromId(chatId),
            text = text,
            replyMarkup = buttons?.toInlineKeyboardMarkup()
        )
    }

    private fun Bot.handleLLMResponse(chatId: Long, llmResponse: LLMResponse) {
        when (llmResponse) {
            is LLMProblemTypeResponse -> {
                sendMessage(
                    chatId = chatId,
                    text = "Определен вид проблемы - ${llmResponse.disProblem.name}",
                    listOf(
                        Button(llmResponse.disProblem.chooseCallbackData, "Продолжить"),
                        Button(CallbackData.NOT_MY_PROBLEM, "Попробовать еще раз")
                    )
                )
            }

            is LLMQuestionResponse -> {
                sendMessage(
                    chatId = chatId,
                    llmResponse.question,
                    listOf(Button(CallbackData.REGENERATE, "\uD83D\uDD04"))
                )
            }
        }
    }
}
