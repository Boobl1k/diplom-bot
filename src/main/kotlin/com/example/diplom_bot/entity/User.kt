package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["chat_id"])]
)
class User(
    val chatId: Long,
    var state: State
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    enum class State {
        WRITING_DESCRIPTION,
        OTHER,

        SENDING_PHONE,

        SENDING_PROBLEM_DETAILS,
        SENDING_SCREENSHOTS,

        UPDATING_PHONE,

        TALKING_WITH_LLM
    }

    var phone: String? = null

    @OneToMany(fetch = FetchType.EAGER)
    val sentProblems: MutableList<UserProblem> = mutableListOf()

    @OneToOne
    var currentProblem: UserProblem? = null
}
