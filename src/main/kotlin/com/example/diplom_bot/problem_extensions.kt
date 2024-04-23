package com.example.diplom_bot

import com.example.diplom_bot.entity.*

val ProblemGroup.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.GROUP}${id}"
val ProblemGroup.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.GROUP}${id}"

val DisProblem.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.DIS_PROBLEM}${id}"
val DisProblem.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.DIS_PROBLEM}${id}"

val Problem.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.PROBLEM}${id}"
val Problem.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.PROBLEM}${id}"

val IASProblem.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.IAS_PROBLEM}${id}"
val IASProblem.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.IAS_PROBLEM}${id}"

val IASModule.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.IAS_MODULE}${id}"
val IASModule.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.IAS_MODULE}${id}"

val IASService.chooseCallbackData
    get() = "${CallbackPrefix.CHOOSE}_${CallbackPrefix.IAS_SERVICE}${id}"
val IASService.goBackCallbackData
    get() = "${CallbackPrefix.GO_BACK}_${CallbackPrefix.IAS_SERVICE}${id}"
