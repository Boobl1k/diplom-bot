package com.example.diplom_bot.model

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem
import com.example.diplom_bot.entity.ProblemGroup

abstract class BotProblemUnit<T>(
    var parent: BotProblemUnit<*>?,
    val name: String,
    val description: String?,
    val children: List<BotProblemUnit<*>>,
    val chooseCallbackData: String,
    val goBackCallbackData: String,
    val entity: T
) {
    companion object {
        const val CHOOSE_CALLBACK_PREFIX = "choose"
        const val GO_BACK_CALLBACK_PREFIX = "back"
    }

    fun addAllConnectedUnitsToList(list: MutableList<BotProblemUnit<*>>) {
        list.add(this)
        children.forEach { it.addAllConnectedUnitsToList(list) }
    }
}

class GroupBotProblemUnit private constructor(
    name: String,
    description: String,
    children: List<BotProblemUnit<*>>,
    chooseCallbackData: String,
    goBackCallbackData: String,
    entity: ProblemGroup
) : BotProblemUnit<ProblemGroup>(null, name, description, children, chooseCallbackData, goBackCallbackData, entity) {
    companion object {
        private const val GROUP_PREFIX = "group"
        fun fromRootProblemGroup(problemGroup: ProblemGroup): BotProblemUnit<ProblemGroup> {
            return GroupBotProblemUnit(
                name = problemGroup.name,
                description = problemGroup.description,
                children = problemGroup.childGroups.map { fromRootProblemGroup(it) } +
                        problemGroup.disProblems.map { DisProblemBotProblemUnit.fromDisProblem(it) },
                chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
                goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
                problemGroup
            ).apply { children.forEach { it.parent = this } }
        }
    }
}

class DisProblemBotProblemUnit private constructor(
    name: String,
    description: String,
    children: List<BotProblemUnit<*>>,
    chooseCallbackData: String,
    goBackCallbackData: String,
    entity: DisProblem
) : BotProblemUnit<DisProblem>(null, name, description, children, chooseCallbackData, goBackCallbackData, entity) {
    companion object {
        private const val DIS_PROBLEM_PREFIX = "dis_problem"
        fun fromDisProblem(disProblem: DisProblem): BotProblemUnit<DisProblem> {
            return DisProblemBotProblemUnit(
                name = disProblem.name,
                description = disProblem.description,
                children = disProblem.problems.map { ProblemBotProblemUnit.fromProblem(it) },
                chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
                goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
                entity = disProblem
            ).apply { children.forEach { it.parent = this } }
        }
    }

    val hasSolution = entity.problems.any { it.solutionText != null }
    val isLeaf = entity.problems.size
}

class ProblemBotProblemUnit private constructor(
    name: String,
    description: String?,
    children: List<BotProblemUnit<*>>,
    chooseCallbackData: String,
    goBackCallbackData: String,
    entity: Problem
) : BotProblemUnit<Problem>(null, name, description, children, chooseCallbackData, goBackCallbackData, entity) {
    companion object {
        private const val PROBLEM_PREFIX = "problem"
        fun fromProblem(problem: Problem): BotProblemUnit<Problem> {
            return ProblemBotProblemUnit(
                name = problem.condition,
                description = problem.description,
                children = listOf(),
                chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
                goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
                entity = problem
            )
        }
    }

    val hasSolution = entity.solutionText != null
}
