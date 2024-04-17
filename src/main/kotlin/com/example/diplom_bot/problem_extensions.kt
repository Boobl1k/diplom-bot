package com.example.diplom_bot

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem
import com.example.diplom_bot.entity.ProblemGroup

val ProblemGroup.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.GROUP}${id}"
val ProblemGroup.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.GROUP}${id}"

val DisProblem.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.DIS_PROBLEM}${id}"
val DisProblem.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.DIS_PROBLEM}${id}"
val DisProblem.sendTicketCallbackData
    get() = "${CallbackPrefix.SEND_TICKET}_${id}"

val Problem.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.PROBLEM}${id}"
val Problem.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.PROBLEM}${id}"
