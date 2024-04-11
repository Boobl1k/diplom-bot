package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class DisProblem(
    @ManyToOne
    val problemGroup: ProblemGroup,
    val name: String,
    val description: String,
    val externalDisProblemId: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "disProblem")
    val problems: List<Problem> = listOf()
}
