package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.model.Word

@Repository
interface WordRepository : JpaRepository<Word, Long> {
    @Query("SELECT d FROM Word d WHERE d.dict.id = :dict_id")
    fun findallWordsInDictBiID(@Param("dict_id") dict_id: Long): List<Word>

    @Query("select max(d.id) from Word d")
    fun maxId(): Long

    /*
    @Query("INSERT INTO  Word (id ,eng, rus, dict.id) select distinct :id, :eng, :rus, :dict_id from Word")
    fun addNewWord(@Param("id") id: Long,
                   @Param("eng") eng: String,
                   @Param("rus") rus: String,
                   @Param("dict_id") dict_id: Long)


     */
}
