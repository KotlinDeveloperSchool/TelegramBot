package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import java.util.*

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Int> {
    fun findAllByOwnerId(id: Long): List<Dictionary>
}
