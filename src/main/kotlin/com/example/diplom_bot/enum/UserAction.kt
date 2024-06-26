package com.example.diplom_bot.enum

import com.example.diplom_bot.CallbackData
import com.example.diplom_bot.CallbackPrefix
import com.example.diplom_bot.entity.User

enum class UserAction {
    GO_WRITE_DESCRIPTION {
        override val callBackPrefix: String
            get() = CallbackData.GO_DESCRIPTION
        override val userStateAfterAction: User.State
            get() = User.State.WRITING_DESCRIPTION
    },
    GO_TALK_WITH_LLM {
        override val callBackPrefix: String
            get() = CallbackData.GO_TALK_WITH_LLM
        override val userStateAfterAction: User.State
            get() = User.State.TALKING_WITH_LLM
    },
    GO_CHOOSE {
        override val callBackPrefix: String
            get() = CallbackData.GO_CHOOSE
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    GO_START {
        override val callBackPrefix: String
            get() = CallbackData.GO_START
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    CHOOSE {
        override val callBackPrefix: String
            get() = CallbackPrefix.CHOOSE
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    GO_BACK {
        override val callBackPrefix: String
            get() = CallbackPrefix.GO_BACK
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    GO_SEND_TICKET {
        override val callBackPrefix: String
            get() = CallbackData.GO_SEND_TICKET
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    BOT_SEND_TICKET {
        override val callBackPrefix: String
            get() = CallbackData.BOT_SEND_TICKET
        override val userStateAfterAction: User.State?
            get() = null
    },
    SEND_TICKET_BY_MYSELF {
        override val callBackPrefix: String
            get() = CallbackData.SEND_TICKET_BY_MYSELF
        override val userStateAfterAction: User.State
            get() = User.State.OTHER
    },
    SEND_TICKET_SURE {
        override val callBackPrefix: String
            get() = CallbackData.SEND_TICKET_SURE
        override val userStateAfterAction: User.State
            get() = User.State.OTHER

    },
    UPDATE_PHONE {
        override val callBackPrefix: String
            get() = CallbackData.UPDATE_PHONE
        override val userStateAfterAction: User.State
            get() = User.State.UPDATING_PHONE
    },
    CONTINUE_TALKING_WITH_LLM {
        override val callBackPrefix: String
            get() = CallbackData.NOT_MY_PROBLEM
        override val userStateAfterAction: User.State
            get() = User.State.TALKING_WITH_LLM
    },
    REGENERATE {
        override val callBackPrefix: String
            get() = CallbackData.REGENERATE
        override val userStateAfterAction: User.State
            get() = User.State.TALKING_WITH_LLM
    };

    abstract val callBackPrefix: String
    abstract val userStateAfterAction: User.State?

    companion object {
        fun getActionFromCallbackQueryData(callbackQueryData: String): UserAction {
            return entries.find { callbackQueryData.startsWith(it.callBackPrefix) }!!
        }
    }
}
