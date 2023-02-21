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
        "select * from dictionary where (owner_id=1 or owner_id=:userId) and name=:name",
        nativeQuery = true
    )
    fun findByNameForUser(@Param("name") name: String, @Param("userId") userId: Long): Optional<Dictionary>

    @Query(
        "SELECT * FROM dictionary WHERE owner_id = :userId Or owner_id = 1",
        nativeQuery = true
    )
    fun findAllDictionariesByUser(@Param("userId") userId: Long): List<Dictionary>

    @Query(
        "SELECT * FROM dictionary d LEFT JOIN favorite f on d.id = f.dict_id " +
                "WHERE f.dict_id IS NULL AND (owner_id = :userId Or owner_id = 1)",
        nativeQuery = true
    )
    fun findNotFavoritesForUser(@Param("userId") userId: Long): List<Dictionary>
}
