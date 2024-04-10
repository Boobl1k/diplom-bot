package com.example.diplom_bot.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class DisProblem(
    @ManyToOne
    val problemGroup: ProblemGroup,
    val name: String,
    val description: String,
    val disProblemId: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
