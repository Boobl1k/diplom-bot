package com.example.diplom_bot.model

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem
import com.example.diplom_bot.entity.ProblemGroup

sealed class BotProblemUnit<T>(
    val parent: BotProblemUnit<*>?,
    val name: String,
    val description: String?,
    val chooseCallbackData: String,
    val goBackCallbackData: String,
    val entity: T
) {
    data class Button(val callBackData: String, val name: String)

    companion object {
        const val CHOOSE_CALLBACK_PREFIX = "choose"
        const val GO_BACK_CALLBACK_PREFIX = "back"

        fun createRootBotProblemUnit(problemGroup: ProblemGroup): BotProblemUnit<*> {
            return GroupBotProblemUnit(problemGroup, null)
        }
    }

    val isRoot: Boolean
        get() = parent == null

    abstract val children: List<BotProblemUnit<*>>
    abstract val headerText: String
    abstract val buttons: List<Button>

    fun getAllConnectedUnits(): List<BotProblemUnit<*>> {
        return mutableListOf<BotProblemUnit<*>>().also { addAllConnectedUnitsToList(it) }
    }

    private fun addAllConnectedUnitsToList(list: MutableList<BotProblemUnit<*>>) {
        list.add(this)
        children.forEach { it.addAllConnectedUnitsToList(list) }
    }

    private class GroupBotProblemUnit(problemGroup: ProblemGroup, parent: BotProblemUnit<*>?) :
        BotProblemUnit<ProblemGroup>(
            parent = parent,
            name = problemGroup.name,
            description = problemGroup.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            problemGroup
        ) {
        companion object {
            const val GROUP_PREFIX = "group"
        }

        override val children: List<BotProblemUnit<*>> = entity.childGroups.map { GroupBotProblemUnit(it, this) } +
                entity.disProblems.map { DisProblemBotProblemUnit(it, this) }

        override val headerText = if (isRoot) "Выберите категорию проблемы:"
        else "Выбрана категория: ${name}\nВыберите категорию проблемы:"

        override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                if (isRoot) listOf() else listOf(Button(goBackCallbackData, "Назад"))
    }

    private open class DisProblemBotProblemUnit(disProblem: DisProblem, parent: BotProblemUnit<*>) :
        BotProblemUnit<DisProblem>(
            parent = parent,
            name = disProblem.name,
            description = disProblem.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
            entity = disProblem
        ) {
        companion object {
            const val DIS_PROBLEM_PREFIX = "dis_problem"
        }

        final override val children: List<BotProblemUnit<*>> = entity.problems.map { ProblemBotProblemUnit(it, this) }

        override val headerText = if (children.isNotEmpty()) "Выбрана проблема: ${name}\nВыберите случай:"
        else "Выбрана проблема: ${name}\nК сожалению, готовых решений для Вас нет." +
                if (entity.enabled) " Хотите создать заявку в ДИС?" else ""

        final override val buttons: List<Button> =
            if (entity.enabled) {
                listOf(
                    if (children.isEmpty()) Button("123", "Создать заявку") // TODO
                    else Button("321", "Другое") // TODO
                )
            } else listOf<Button>() +
                    listOf(Button(goBackCallbackData, "Назад"))
    }

    private class ProblemBotProblemUnit(problem: Problem, parent: BotProblemUnit<*>) : BotProblemUnit<Problem>(
        parent = parent,
        name = problem.condition,
        description = problem.description,
        chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
        goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
        entity = problem
    ) {
        companion object {
            const val PROBLEM_PREFIX = "problem"
        }

        override val children: List<BotProblemUnit<*>> = listOf()

        override val headerText =
            "Выбрана проблема: ${parent.name}\nВыбран случай: ${name}\nВозможное решение:\n\n${entity.solutionText}"

        override val buttons: List<Button> = if (entity.disProblem.enabled) listOf(
            Button("123", "Решение не помогло. Создать заявку в ДИС")
        ) else listOf<Button>() +
                listOf(Button(goBackCallbackData, "Назад"))
    }
}
