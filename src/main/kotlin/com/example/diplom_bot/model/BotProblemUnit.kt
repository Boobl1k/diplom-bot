package com.example.diplom_bot.model

import com.example.diplom_bot.CallbackData
import com.example.diplom_bot.chooseCallbackData
import com.example.diplom_bot.entity.*
import com.example.diplom_bot.goBackCallbackData

sealed class BotProblemUnit<T>(
    val parent: BotProblemUnit<*>?,
    val name: String,
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

    class GroupBotProblemUnit(problemGroup: ProblemGroup, parent: BotProblemUnit<*>?) :
        BotProblemUnit<ProblemGroup>(
            parent = parent,
            name = problemGroup.name,
            chooseCallbackData = problemGroup.chooseCallbackData,
            goBackCallbackData = problemGroup.goBackCallbackData,
            problemGroup
        ) {
        override val children: List<BotProblemUnit<*>> = entity.childGroups.map { GroupBotProblemUnit(it, this) } +
                entity.disProblems.map {
                    if (it is IASProblem) IASProblemBotProblemUnit(it, this)
                    else DisProblemBotProblemUnit(it, this)
                }

        override val headerText = if (isRoot) "Выберите категорию проблемы:"
        else "Выбрана категория: ${name}\nВыберите категорию проблемы:"

        override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                if (isRoot) listOf() else listOf(
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    class DisProblemBotProblemUnit(disProblem: DisProblem, parent: BotProblemUnit<*>) :
        BotProblemUnit<DisProblem>(
            parent = parent,
            name = disProblem.name,
            chooseCallbackData = disProblem.chooseCallbackData,
            goBackCallbackData = disProblem.goBackCallbackData,
            entity = disProblem
        ) {
        override val children: List<BotProblemUnit<*>> = entity.problems.map { ProblemBotProblemUnit(it, this) }

        override val headerText = if (children.isNotEmpty()) "Выбрана проблема: ${name}\nВыберите случай"
        else "Выбрана проблема: ${name}\nК сожалению, готовых решений для Вас нет."

        override val buttons: List<Button> = children.map { Button(it.chooseCallbackData, it.name) } +
                listOf(
                    Button(CallbackData.GO_SEND_TICKET, "Хочу создать заявку"),
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    class ProblemBotProblemUnit(problem: Problem, parent: DisProblemBotProblemUnit) : BotProblemUnit<Problem>(
        parent = parent,
        name = problem.condition,
        chooseCallbackData = problem.chooseCallbackData,
        goBackCallbackData = problem.goBackCallbackData,
        entity = problem
    ) {
        override val children: List<BotProblemUnit<*>> = listOf()

        override val headerText =
            "Выбрана проблема: ${parent.name}\nВыбран случай: ${name}\nВозможное решение:\n\n${entity.solutionText}"

        override val buttons: List<Button> = listOf(
            Button(CallbackData.GO_SEND_TICKET, "Хочу создать заявку"),
            Button(goBackCallbackData, "Назад"),
            Button(CallbackData.GO_START, "В начало")
        )
    }

    class IASProblemBotProblemUnit(iasProblem: IASProblem, parent: BotProblemUnit<*>) :
        BotProblemUnit<IASProblem>(
            parent = parent,
            name = iasProblem.name,
            chooseCallbackData = iasProblem.chooseCallbackData,
            goBackCallbackData = iasProblem.goBackCallbackData,
            entity = iasProblem
        ) {
        override val children = entity.iasModules.map { IASModuleBotProblemUnit(it, this) }

        override val headerText = "Выбрана проблема ${name}. Выберите модуль"

        override val buttons = children.map { Button(it.chooseCallbackData, it.name) } +
                listOf(
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    class IASModuleBotProblemUnit(iasModule: IASModule, parent: BotProblemUnit<*>) :
        BotProblemUnit<IASModule>(
            parent = parent,
            name = iasModule.name,
            chooseCallbackData = iasModule.chooseCallbackData,
            goBackCallbackData = iasModule.goBackCallbackData,
            entity = iasModule
        ) {
        override val children =
            if (entity.services.size > 1) entity.services.map { IASServiceBotProblemUnit(it, this) } else listOf()

        override val headerText =
            if (children.size > 1) "Выбран модуль ${name}. Выберите сервис" else "Выбран модуль $name"

        override val buttons = children.map { Button(it.chooseCallbackData, it.name) } +
                (if (children.size > 1) listOf() else listOf(Button(CallbackData.GO_SEND_TICKET, "Создать заявку"))) +
                listOf(
                    Button(goBackCallbackData, "Назад"),
                    Button(CallbackData.GO_START, "В начало")
                )
    }

    class IASServiceBotProblemUnit(iasService: IASService, parent: IASModuleBotProblemUnit) :
        BotProblemUnit<IASService>(
            parent = parent,
            name = iasService.name ?: "",
            chooseCallbackData = iasService.chooseCallbackData,
            goBackCallbackData = iasService.goBackCallbackData,
            entity = iasService
        ) {
        override val children: List<BotProblemUnit<*>> = listOf()

        override val headerText = "Выбран модуль ${parent.entity.name}. Выбран сервис ${entity.name}"

        override val buttons: List<Button> = listOf(
            Button(CallbackData.GO_SEND_TICKET, "Создать заявку"),
            Button(goBackCallbackData, "Назад"),
            Button(CallbackData.GO_START, "В начало")
        )
    }
}
