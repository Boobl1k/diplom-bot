package com.example.diplom_bot.repository;

import com.example.diplom_bot.entity.UserProblem
import org.springframework.data.jpa.repository.JpaRepository

interface UserProblemRepository : JpaRepository<UserProblem, Long>
