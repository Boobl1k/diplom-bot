package com.example.diplom_bot.model

import com.example.diplom_bot.CallbackData
import com.example.diplom_bot.chooseCallbackData
import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem
import com.example.diplom_bot.entity.ProblemGroup
import com.example.diplom_bot.goBackCallbackData

sealed class BotProblemUnit<T>(
    val parent: BotProblemUnit<*>?,
    val name: String,
    val description: String?,
    val chooseCallbackData: String,
    val goBackCallbackData: String,
    val entity: T
) {
    companion object {
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
            chooseCallbackData = problemGroup.chooseCallbackData,
            goBackCallbackData = problemGroup.goBackCallbackData,
            problemGroup
        ) {
        override val children: List<BotProblemUnit<*>> = entity.childGroups.map { GroupBotProblemUnit(it, this) } +
                entity.disProblems.map { DisProblemBotProblemUnit(it, this) }

        override val headerText = if (isRoot) "Выберите категорию проблемы:"
        else "Выбрана категория: ${name}\nВыберите категорию проблемы:"

        override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                if (isRoot) listOf() else listOf(
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    private open class DisProblemBotProblemUnit(disProblem: DisProblem, parent: BotProblemUnit<*>) :
        BotProblemUnit<DisProblem>(
            parent = parent,
            name = disProblem.name,
            description = disProblem.description,
            chooseCallbackData = disProblem.chooseCallbackData,
            goBackCallbackData = disProblem.goBackCallbackData,
            entity = disProblem
        ) {
        final override val children: List<BotProblemUnit<*>> = entity.problems.map { ProblemBotProblemUnit(it, this) }

        override val headerText = if (children.isNotEmpty()) "Выбрана проблема: ${name}\nВыберите случай:"
        else "Выбрана проблема: ${name}\nК сожалению, готовых решений для Вас нет."

        final override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                listOf(
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    private class ProblemBotProblemUnit(problem: Problem, parent: BotProblemUnit<*>) : BotProblemUnit<Problem>(
        parent = parent,
        name = problem.condition,
        description = problem.description,
        chooseCallbackData = problem.chooseCallbackData,
        goBackCallbackData = problem.goBackCallbackData,
        entity = problem
    ) {
        override val children: List<BotProblemUnit<*>> = listOf()

        override val headerText =
            "Выбрана проблема: ${parent.name}\nВыбран случай: ${name}\nВозможное решение:\n\n${entity.solutionText}"

        override val buttons: List<Button> = listOf(
            Button(goBackCallbackData, "Назад"),
            Button(CallbackData.GO_START, "В начало")
        )
    }
}
