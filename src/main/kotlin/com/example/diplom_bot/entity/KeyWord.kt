package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class KeyWord(
    val keyWord: String,
    val weight: Int,
    @ManyToOne
    val disProblem: DisProblem
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
