package com.example.diplom_bot

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem
import com.example.diplom_bot.entity.ProblemGroup

const val GO_START_CALLBACK_DATA = "go_start"

const val CHOOSE_CALLBACK_PREFIX = "choose"
const val GO_BACK_CALLBACK_PREFIX = "back"

const val GROUP_PREFIX = "group"
const val DIS_PROBLEM_PREFIX = "dis_problem"
const val PROBLEM_PREFIX = "problem"


val ProblemGroup.chooseCallbackData
    get() = "${CHOOSE_CALLBACK_PREFIX}_${GROUP_PREFIX}${id}"
val ProblemGroup.goBackCallbackData
    get() = "${GO_BACK_CALLBACK_PREFIX}_${GROUP_PREFIX}${id}"

val DisProblem.chooseCallbackData
    get() = "${CHOOSE_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${id}"
val DisProblem.goBackCallbackData
    get() = "${GO_BACK_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${id}"

val Problem.chooseCallbackData
    get() = "${CHOOSE_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${id}"
val Problem.goBackCallbackData
    get() = "${GO_BACK_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${id}"
