package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
open class DisProblem(
    @ManyToOne
    open val problemGroup: ProblemGroup,
    open var name: String,
    open val description: String,
    open val externalDisProblemId: Int,
    open var enabled: Boolean,
    open val llmName: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long? = null

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "disProblem")
    open val problems: List<Problem> = listOf()

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "disProblem")
    open val keyWords: List<KeyWord> = listOf()
}
