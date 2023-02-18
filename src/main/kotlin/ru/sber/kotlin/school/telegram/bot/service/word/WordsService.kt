package ru.sber.kotlin.school.telegram.bot.service.word

import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import java.util.regex.Pattern

@Service
class WordsService(
    val dictionaryRep: DictionaryRepository,
    val wordRep: WordRepository
) {

    fun createNewWord(dictionary_id: Long, word: String) {
        //ситуации когда словаря нет, быть не может
        val dictionary = dictionaryRep.findById(dictionary_id).get()

        val rusNEng = word.split(Pattern.compile("-"), 2)
        val rus = rusNEng[0].trim()
        val eng = rusNEng[2].trim()
        val wordEntity = Word(rus, eng, dictionary)
        wordRep.saveAndFlush(wordEntity)
    }

    fun createNewWords(dictionary_id: Long, words: List<String>) {
        //ситуации когда словаря нет, быть не может
        val dictionary = dictionaryRep.findById(dictionary_id).get()
        val rusNEng: List<Pair<String, String>> =
            words.map { word -> word.split(Pattern.compile("-"), 2) }
                .map {Pair(it[2].trim(), it[1].trim()) }

        val wordEntity = rusNEng.map { Word(it.first, it.second, dictionary) }
        wordRep.saveAllAndFlush(wordEntity)
    }
}