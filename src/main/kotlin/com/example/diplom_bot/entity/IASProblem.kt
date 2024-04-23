package com.example.diplom_bot.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany

@Entity(name = "ias_problem")
class IASProblem(
    problemGroup: ProblemGroup,
    name: String,
    description: String,
    externalDisProblemId: Int,
    enabled: Boolean,
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "problem")
    val iasModules: List<IASModule>
) : DisProblem(problemGroup, name, description, externalDisProblemId, enabled)
