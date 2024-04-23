package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity(name = "ias_service")
class IASService(
    val name: String?,
    val groupNeeded: Boolean,
    val applicantName: String?,
    val additionalInfo: String?,
    @ManyToOne
    val iasModule: IASModule
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
