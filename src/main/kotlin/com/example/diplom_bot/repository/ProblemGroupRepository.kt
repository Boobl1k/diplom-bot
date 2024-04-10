package com.example.diplom_bot.repository

import com.example.diplom_bot.entity.ProblemGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProblemGroupRepository : JpaRepository<ProblemGroup, Long> {
    @Query("select x from ProblemGroup x where x.parentGroup is NULL")
    fun findRootGroups(): List<ProblemGroup>
}
