package com.example.diplom_bot

import com.example.diplom_bot.entity.IASProblem
import com.example.diplom_bot.entity.User
import com.example.diplom_bot.enum.UserAction
import com.example.diplom_bot.model.BotProblemUnit
import com.example.diplom_bot.model.Button
import com.example.diplom_bot.property.ChatBotProperties
import com.example.diplom_bot.repository.ProblemGroupRepository
import com.example.diplom_bot.service.DisProblemService
import com.example.diplom_bot.service.KeyWordService
import com.example.diplom_bot.service.UserProblemService
import com.example.diplom_bot.service.UserService
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
    private val txTemplate: TransactionTemplate
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
                    bot.sendStartMessage(ChatId.fromId(message.chat.id))
                }

                myCommand("solvemyproblem") {
                    bot.sendStartMessage(ChatId.fromId(message.chat.id))
                }

                myCommand("reload") {
                    loadProblemUnits()
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Бот был перезагружен")
                }

                myCommand("updatemyinfo") { user ->
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = """
                            Ваши данные:
                            ФИО - ${user.name ?: ""}
                            Телефон - ${user.phone ?: ""}
                            Адрес - ${user.address ?: ""}
                            Номер кабинета - ${user.officeNumber ?: ""}
                        """.trimIndent(),
                        replyMarkup = listOf(
                            Button(CallbackData.UPDATE_NAME, "Обновить ФИО"),
                            Button(CallbackData.UPDATE_PHONE, "Обновить номер телефон"),
                            Button(CallbackData.UPDATE_ADDRESS, "Обновить рабочий адрес"),
                            Button(CallbackData.UPDATE_OFFICE_NUMBER, "Обновить рабочий номер кабинета")
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
                            val unit = allUnits.find { it.goBackCallbackData == callbackQuery.data }!!
                            if (unit is BotProblemUnit.GroupBotProblemUnit) {
                                userProblemService.clearCurrentProblem(user)
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
                            bot.sendStartMessage(ChatId.fromId(callbackQuery.message?.chat?.id!!))
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
                            val fileId = user.currentProblem!!.fileId
                            if (fileId != null) {
                                val file = File.createTempFile("user_screenshot", "")
                                FileUtils.writeByteArrayToFile(file, bot.downloadFileBytes(fileId)!!)
                                userProblemService.sendProblemToSupport(user, file)
                            } else {
                                userProblemService.sendProblemToSupport(user)
                            }
                            sendMessage(
                                "Ваша заявка отправлена. Ожидайте звонка",
                                listOf(Button(CallbackData.GO_START, "В начало"))
                            )
                        }

                        UserAction.UPDATE_NAME -> {
                            sendMessage("Пожалуйста, напишите свои ФИО")
                        }

                        UserAction.UPDATE_PHONE -> {
                            sendMessage("Пожалуйста, напишите свой номер телефона")
                        }

                        UserAction.UPDATE_ADDRESS -> {
                            sendMessage("Пожалуйста, напишите свой рабочий адрес")
                        }

                        UserAction.UPDATE_OFFICE_NUMBER -> {
                            sendMessage("Пожалуйста, напишите свой рабочий номер кабинета")
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

                        User.State.SENDING_NAME -> {
                            user.name = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваше имя"
                            )
                            bot.handleBotSendingTicket(user)
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

                        User.State.SENDING_ADDRESS -> {
                            user.address = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш адрес"
                            )
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_OFFICE_NUMBER -> {
                            user.officeNumber = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш номер кабинета"
                            )
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_PROBLEM_DETAILS -> {
                            userProblemService.getCurrentProblem(user).details = text
                            bot.handleBotSendingTicket(user)
                        }

                        User.State.SENDING_SCREENSHOTS -> {}

                        User.State.UPDATING_NAME -> {
                            user.state = User.State.OTHER
                            user.name = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваше имя"
                            )
                        }

                        User.State.UPDATING_PHONE -> {
                            user.state = User.State.OTHER
                            user.phone = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш номер",
                                replyMarkup = ReplyKeyboardRemove()
                            )
                        }

                        User.State.UPDATING_ADDRESS -> {
                            user.state = User.State.OTHER
                            user.address = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш адрес"
                            )
                        }

                        User.State.UPDATING_OFFICE_NUMBER -> {
                            user.state = User.State.OTHER
                            user.officeNumber = text
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = "Я записал Ваш номер кабинета"
                            )
                        }

                        User.State.OTHER -> {
                            bot.sendMessage(
                                chatId = ChatId.fromId(user.chatId),
                                text = """
                                    Мои команды:
                                    1. /start - старт
                                    2. /solvemyproblem - попросить бота помочь решить Вашу проблему
                                    3. /updatemyinfo - обновить Ваши данные
                                """.trimIndent()
                            )
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
                    bot.handleBotSendingTicket(user)
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

    private fun Bot.sendStartMessage(chatId: ChatId.Id) {
        sendMessage(
            chatId = chatId,
            text = """
                Я предлагаю Вам 2 варианта определения проблемы:
                1. Вы вводите описание проблемы, затем я предложу вам несколько видов проблем, Вы выберите свою
                2. Я вам предлагаю категории, Вы выбираете нужную
            """.trimIndent(),
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
