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
            return GroupBotProblemUnit(problemGroup)
        }
    }

    fun getAllConnectedUnits(): List<BotProblemUnit<*>> {
        return mutableListOf<BotProblemUnit<*>>().also { addAllConnectedUnitsToList(it) }
    }

    private fun addAllConnectedUnitsToList(list: MutableList<BotProblemUnit<*>>) {
        list.add(this)
        children.forEach { it.addAllConnectedUnitsToList(list) }
    }

    abstract val children: List<BotProblemUnit<*>>
    abstract val headerText: String
    abstract val buttons: List<Button>

    val isRoot: Boolean
        get() = parent == null

    private class GroupBotProblemUnit(
        parent: BotProblemUnit<*>?,
        name: String,
        description: String,
        chooseCallbackData: String,
        goBackCallbackData: String,
        entity: ProblemGroup
    ) : BotProblemUnit<ProblemGroup>(parent, name, description, chooseCallbackData, goBackCallbackData, entity) {
        companion object {
            const val GROUP_PREFIX = "group"
        }

        constructor(problemGroup: ProblemGroup, parent: BotProblemUnit<*>) : this(
            parent = parent,
            name = problemGroup.name,
            description = problemGroup.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            problemGroup
        )

        constructor(problemGroup: ProblemGroup) : this(
            parent = null,
            name = problemGroup.name,
            description = problemGroup.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_$GROUP_PREFIX${problemGroup.id}",
            problemGroup
        )

        override val children: List<BotProblemUnit<*>> = entity.childGroups.map { GroupBotProblemUnit(it, this) } +
                entity.disProblems.map { DisProblemBotProblemUnit(it, this) }

        override val headerText = if (isRoot) "Выберите категорию проблемы:"
        else "Выбрана категория: ${name}\nВыберите категорию проблемы:"

        override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                if (isRoot) listOf() else listOf(Button(goBackCallbackData, "Назад"))
    }

    private class DisProblemBotProblemUnit(
        parent: BotProblemUnit<*>,
        name: String,
        description: String,
        chooseCallbackData: String,
        goBackCallbackData: String,
        entity: DisProblem
    ) : BotProblemUnit<DisProblem>(parent, name, description, chooseCallbackData, goBackCallbackData, entity) {
        companion object {
            const val DIS_PROBLEM_PREFIX = "dis_problem"
        }

        constructor(disProblem: DisProblem, parent: BotProblemUnit<*>) : this(
            parent = parent,
            name = disProblem.name,
            description = disProblem.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${DIS_PROBLEM_PREFIX}${disProblem.id}",
            entity = disProblem
        )

        override val children: List<BotProblemUnit<*>> = entity.problems.map { ProblemBotProblemUnit(it, this) }

        override val headerText = if (children.isNotEmpty()) "Выбрана проблема: ${name}\nВыберите случай:"
        else "Выбрана проблема: ${name}\nК сожалению, готовых решений для Вас нет. Хотите создать заявку в ДИС?"

        override val buttons: List<Button> = if (children.isEmpty()) listOf(
            Button("123", "Создать заявку"), // TODO
            Button(goBackCallbackData, "Назад")
        ) else children.map { Button(it.chooseCallbackData, it.name) } + listOf(
            Button("321", "Другое"),
            Button(goBackCallbackData, "Назад")
        )
    }

    private class ProblemBotProblemUnit(
        parent: BotProblemUnit<*>,
        name: String,
        description: String?,
        chooseCallbackData: String,
        goBackCallbackData: String,
        entity: Problem,
    ) : BotProblemUnit<Problem>(parent, name, description, chooseCallbackData, goBackCallbackData, entity) {
        companion object {
            const val PROBLEM_PREFIX = "problem"
        }

        constructor(problem: Problem, parent: BotProblemUnit<*>) : this(
            parent = parent,
            name = problem.condition,
            description = problem.description,
            chooseCallbackData = "${CHOOSE_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
            goBackCallbackData = "${GO_BACK_CALLBACK_PREFIX}_${PROBLEM_PREFIX}${problem.id}",
            entity = problem
        )

        override val children: List<BotProblemUnit<*>> = listOf()

        override val headerText =
            "Выбрана проблема: ${parent.name}\nВыбран случай: ${name}\nВозможное решение:\n\n${entity.solutionText}"

        override val buttons: List<Button> = listOf(
            Button("123", "Решение не помогло. Создать заявку в ДИС"),
            Button(goBackCallbackData, "Назад")
        )
    }
}
