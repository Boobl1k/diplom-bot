package com.example.diplom_bot.entity

import jakarta.persistence.*

@Entity
class UserProblem(
    @ManyToOne
    val user: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var sent: Boolean = false
    var shortDescription: String? = null

    @ManyToOne
    var disProblem: DisProblem? = null

    @ManyToOne
    var problemCase: Problem? = null
    var details: String? = null
    var ticketId: Long? = null

    @ManyToOne
    var iasService: IASService? = null

    @ManyToOne
    var iasModule: IASModule? = null

    var fileId: String? = null

    var fileName: String? = null
}
