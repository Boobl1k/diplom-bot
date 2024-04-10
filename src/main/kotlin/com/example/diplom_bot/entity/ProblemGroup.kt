package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class ProblemGroup(
    @ManyToOne
    val parentGroup: ProblemGroup?,
    val name: String,
    val description: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parentGroup")
    val childGroups: List<ProblemGroup> = listOf()

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "problemGroup")
    val disProblems: List<DisProblem> = listOf()
}
