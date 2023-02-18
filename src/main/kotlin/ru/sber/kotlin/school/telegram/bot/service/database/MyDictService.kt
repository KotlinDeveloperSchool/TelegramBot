package ru.sber.kotlin.school.telegram.bot.service.database

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.objects.MessageContext
import ru.sber.kotlin.school.telegram.bot.model.*
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import java.util.*

@Service
class MyDictService (
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository
) {
    fun getfindAllMyDictionary(userId: Long): List<Dictionary> {
        val user = userRepository.findById(userId).get()
        return dictionaryRepository.findAllDictionariesByUser(user)
    }
    /**
     * Функция получения списка слов в моем словаре
     */
    fun getAllWordsInMyDict(userId: Long, theme: String): List<Word> {
        val user = userRepository.findById(userId).get()
        var best_id: Long = -99999
        dictionaryRepository.findDictionaryByTheme(user, theme).forEach { best_id = it.id }
        return wordRepository.findallWordsInDictBiID(best_id)

    }
    /**
     * Функция добавление новых слов в словарь
     */

    fun addNewWordInDict(userId: Long, theme: String, eng: String, rus: String) {
        val user = userRepository.findById(userId).get()
        val maxId = wordRepository.maxId()
        val new_dict = dictionaryRepository.findDictionaryByTheme(user, theme).first()
        Word(id = maxId,
             rus = rus,
             eng = eng,
             dict = new_dict).let { wordRepository.save(it) }


        /*
        wordRepository.addNewWord(id=maxId,
                                  eng = eng,
                                  rus = rus,
                                  dict_id = best_id)


         */

    }

}