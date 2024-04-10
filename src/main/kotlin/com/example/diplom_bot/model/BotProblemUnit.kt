package com.example.diplom_bot.model

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.ProblemGroup

abstract class BotProblemUnit(
    var parent: BotProblemUnit?,
    val name: String,
    val description: String,
    val children: List<BotProblemUnit>,
    val chooseCallbackData: String,
    val goBackCallbackData: String
) {
    companion object {
        const val CHOOSE_CALLBACK_PREFIX = "choose"
        const val GO_BACK_CALLBACK_PREFIX = "back"
    }

    fun addAllConnectedUnitsToList(list: MutableList<BotProblemUnit>) {
        val deque = ArrayDeque<BotProblemUnit>()
        deque.add(this)
        while (deque.isNotEmpty()) {
            val unit = deque.removeFirst()
            list.add(unit)
            deque.addAll(unit.children)
        }
    }
}

class GroupBotProblemUnit private constructor(
    name: String,
    description: String,
    children: List<BotProblemUnit>,
    chooseCallbackData: String,
    goBackCallbackData: String
) : BotProblemUnit(null, name, description, children, chooseCallbackData, goBackCallbackData) {
    companion object {
        const val GROUP_PREFIX = "group"
        fun fromRootProblemGroup(problemGroup: ProblemGroup): BotProblemUnit {
            return GroupBotProblemUnit(
                name = problemGroup.name,
                description = problemGroup.description,
                children = problemGroup.childGroups.map { fromRootProblemGroup(it) } +
                        problemGroup.disProblems.map { DisProblemBotProblemUnit.fromDisProblemAndGroup(it) },
                chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
                goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}"
            ).also { unit -> unit.children.forEach { it.parent = unit } }
        }
    }
}

class DisProblemBotProblemUnit private constructor(
    name: String,
    description: String,
    children: List<BotProblemUnit>,
    chooseCallbackData: String,
    goBackCallbackData: String
) : BotProblemUnit(null, name, description, children, chooseCallbackData, goBackCallbackData) {
    companion object {
        const val DIS_PROBLEM_PREFIX = "problem"
        fun fromDisProblemAndGroup(disProblem: DisProblem): BotProblemUnit {
            return DisProblemBotProblemUnit(
                name = disProblem.name,
                description = disProblem.description,
                children = listOf(), //TOOD
                chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
                goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
            )
        }
    }
}
