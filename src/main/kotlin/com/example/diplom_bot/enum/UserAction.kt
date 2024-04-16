package com.example.diplom_bot.enum

import com.example.diplom_bot.CallbackData
import com.example.diplom_bot.CallbackPrefix

enum class UserAction {
    GO_WRITE_DESCRIPTION {
        override val callBackPrefix: String
            get() = CallbackData.GO_DESCRIPTION
    },
    GO_CHOOSE {
        override val callBackPrefix: String
            get() = CallbackData.GO_CHOOSE
    },
    GO_START {
        override val callBackPrefix: String
            get() = CallbackData.GO_START
    },
    CHOOSE {
        override val callBackPrefix: String
            get() = CallbackPrefix.CHOOSE
    },
    GO_BACK {
        override val callBackPrefix: String
            get() = CallbackPrefix.GO_BACK
    };

    abstract val callBackPrefix: String

    companion object {
        fun getActionFromCallbackQueryData(callbackQueryData: String): UserAction {
            return entries.find { callbackQueryData.startsWith(it.callBackPrefix) }!!
        }
    }
}
