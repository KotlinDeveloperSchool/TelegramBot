package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Word

@Repository
interface WordRepository : JpaRepository<Word, Long> {

    @Query(
        "select id, rus, eng, dict_id from word where dict_id=:dictId",
        nativeQuery = true
    )
    fun findAllByDictionaryId(@Param("dictId") dictionaryId: Long): List<Word>

    fun findAllByIdIn(ids: ArrayList<Long>): List<Word>
}
