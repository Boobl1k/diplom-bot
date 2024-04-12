package com.example.diplom_bot.entity

import jakarta.persistence.*

// TODO unused
@Entity
class SolutionStep(
    val stepNumber: Int,
    val text: String,
    @Lob
    val picture: Array<Byte>,
    @ManyToOne
    val problem: Problem
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}
