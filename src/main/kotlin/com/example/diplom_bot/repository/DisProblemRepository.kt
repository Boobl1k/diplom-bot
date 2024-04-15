package com.example.diplom_bot.repository

import com.example.diplom_bot.entity.DisProblem
import org.springframework.data.jpa.repository.JpaRepository

interface DisProblemRepository : JpaRepository<DisProblem, Long>
