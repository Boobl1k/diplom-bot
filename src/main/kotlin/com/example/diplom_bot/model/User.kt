package com.example.diplom_bot.model

import com.example.diplom_bot.entity.DisProblem
import com.example.diplom_bot.entity.Problem

class User(
    val chatId: Long,
    var state: State
) {
    enum class State {
        WRITING_DESCRIPTION,
        OTHER,
        SENDING_NAME,
        SENDING_PHONE,
        SENDING_ADDRESS,
        SENDING_OFFICE_NUMBER,
        SENDING_PROBLEM_DETAILS
    }

    var phone: String? = null
    var name: String? = null
    var address: String? = null
    var officeNumber: String? = null
    private val problems: MutableList<UserProblem> = mutableListOf()
    private var currentProblem: UserProblem? = null

    fun createNewProblem(): UserProblem {
        val problem = UserProblem()
        currentProblem = problem
        problems.add(problem)
        return problem
    }

    fun getCurrentProblem(): UserProblem {
        return currentProblem ?: createNewProblem()
    }

    fun clearCurrentProblem() {
        currentProblem = null
    }
}

class UserProblem {
    var sent: Boolean = false
    var shortDescription: String? = null
    var disProblem: DisProblem? = null
    var problemCase: Problem? = null
    var details: String? = null
    var ticketId: Long? = null
}
