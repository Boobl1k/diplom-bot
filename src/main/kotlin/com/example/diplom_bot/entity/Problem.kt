package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class Problem(
    @ManyToOne
    val disProblem: DisProblem,
    val solutionText: String?,
    @Lob
    val solutionPicture: Array<Byte>,
    val condition: String,
    val description: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
