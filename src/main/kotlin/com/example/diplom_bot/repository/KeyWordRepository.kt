package com.example.diplom_bot.repository

import com.example.diplom_bot.entity.KeyWord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KeyWordRepository : JpaRepository<KeyWord, Long>
