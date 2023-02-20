package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import java.util.Optional

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    @Query(
        "select * from dictionary where (owner_id=1 or owner_id=:user) and name=:name",
        nativeQuery = true
    )
    fun findByNameForUser(@Param("name") name: String, @Param("user") userId: Long): Optional<Dictionary>

    @Query(
        "SELECT * FROM dictionary WHERE owner_id = :userId Or owner_id = 1",
        nativeQuery = true
    )
    fun findAllDictionariesByUser(@Param("user") userId: Long): List<Dictionary>
}
