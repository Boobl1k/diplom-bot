package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity(name = "ias_module")
class IASModule(
    val name: String,
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "iasModule")
    val services: List<IASService>,
    @ManyToOne
    val problem: IASProblem
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
