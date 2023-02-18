package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import java.util.Optional

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    @Query(
        "select id from dictionary where (owner_id=1 or owner_id=:user) and name=:name",
        nativeQuery = true
    )
    fun findIdByNameForUser(@Param("name") name: String, @Param("user") userId: Long): Optional<Long>
}
