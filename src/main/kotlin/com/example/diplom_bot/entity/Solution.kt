package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class Solution(
    val text: String?,
    val picture: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @OneToMany(fetch = FetchType.EAGER)
    val solutionSteps: List<SolutionStep> = listOf()
}
