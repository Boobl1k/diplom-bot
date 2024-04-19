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

        SENDING_NAME,
        SENDING_PHONE,
        SENDING_ADDRESS,
        SENDING_OFFICE_NUMBER,

        SENDING_PROBLEM_DETAILS,

        UPDATING_NAME,
        UPDATING_PHONE,
        UPDATING_ADDRESS,
        UPDATING_OFFICE_NUMBER
    }

    var phone: String? = null
    var name: String? = null
    var address: String? = null
    var officeNumber: String? = null

    @OneToMany(fetch = FetchType.EAGER)
    val sentProblems: MutableList<UserProblem> = mutableListOf()

    @OneToOne
    var currentProblem: UserProblem? = null
}
