package com.example.diplom_bot.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class SolutionStep(
    val stepNumber: Int,
    val text: String,
    val picture: String?,
    @ManyToOne
    val solution: Solution
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}
