package ru.sber.kotlin.school.telegram.bot.game

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.sber.kotlin.school.telegram.bot.exception.ActionException
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import kotlin.random.Random

@Component("FreeType")
class FreeTypeGameService(
    private val wordRepository: WordRepository,
    private val botRedisRepository: BotRedisRepository
) : GameService {
    private val DELIMITER = "#"
    private val PAIR_TEMPLATE = "%d$DELIMITER%s$DELIMITER%s"
    private val WORD_COUNT = 5

    override fun prepare(userId: Long) {
        val dictionaryId = (botRedisRepository.getDictionary(userId)
            ?: throw ActionException("No chosen dictionary by user $userId")).toLong()
        val allWords = wordRepository.findAllByDictionaryId(dictionaryId)
        val wordsToLearn = getRandomWords(WORD_COUNT, allWords)

        wordsToLearn.forEach {
            if (Random.nextInt(2) == 0)
                botRedisRepository.leftPushMsg(userId, PAIR_TEMPLATE.format(it.id, it.rus, it.eng))
            else
                botRedisRepository.leftPushMsg(userId, PAIR_TEMPLATE.format(it.id, it.eng, it.rus))
        }
    }

    override fun getWordsForLearning(userId: Long): String =
        defaultAllWordsFromDic(userId, DELIMITER, botRedisRepository, wordRepository)

    private fun getRandomWords(count: Int, words: List<Word>): MutableSet<Word> {
        val result = HashSet<Word>()
        while (result.size < count && result.size < words.size) {
            val i = Random.nextInt(words.size)
            result.add(words[i])
        }
        return result
    }


    override fun getTextForRound(userId: Long): String {
        val word = botRedisRepository.rightPopMsg(userId)
        if (word == null) {
            botRedisRepository.deleteState(userId)
            return "Тренировка окончена! Но вы можете начать сначала..."
        }
        botRedisRepository.putAnswer(userId, word)

        return word.split(DELIMITER)[1]
    }

    override fun getKeyboardForRound(userId: Long): ReplyKeyboard =
        ReplyKeyboardRemove(true)


    override fun checkAnswer(upd: Update): String =
        defaultCheck(upd, DELIMITER, botRedisRepository)
}
