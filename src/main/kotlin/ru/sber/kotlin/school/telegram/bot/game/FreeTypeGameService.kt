package ru.sber.kotlin.school.telegram.bot.game

import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.objects.Update
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
    private val DELIMITER = '#'
    private val PAIR_TEMPLATE = "%s$DELIMITER%s"

    override fun prepare(userId: Long) {
        val dictionaryId = (botRedisRepository.getDictionary(userId)
            ?: throw ActionException("No chosen dictionary by user $userId")).toLong()
        val allWords = wordRepository.findAllByDictionaryId(dictionaryId)
        val wordsToLearn = getRandomWords(3, allWords)

        wordsToLearn.forEach {
            if (Random.nextInt(2) == 0)
                botRedisRepository.leftPushMsg(userId, PAIR_TEMPLATE.format(it.rus, it.eng))
            else
                botRedisRepository.leftPushMsg(userId, PAIR_TEMPLATE.format(it.eng, it.rus))
        }
    }

    private fun getRandomWords(count: Int, words: List<Word>): MutableSet<Word> {
        val result = HashSet<Word>()
        while (result.size < count) {
            val i = Random.nextInt(words.size)
            result.add(words[i])
        }
        return result
    }


    override fun startRound(userId: Long): String {
        val word = botRedisRepository.rightPopMsg(userId)
        if (word == null) {
            botRedisRepository.deleteState(userId)
            return "Тренировка окончена! Но вы можете начать сначала..."
        }
        botRedisRepository.putAnswer(userId, word)

        return word.split(DELIMITER)[0]
    }

    override fun checkAnswer(upd: Update): String {
        val answer = upd.message.text
        val userId = getUser(upd).id
        val word = (botRedisRepository.getAnswer(userId)
            ?: throw ActionException("No question for user $userId"))
        val qa = word.split(DELIMITER)

        val result: String
        if (answer.equals(qa[1], true)) {
            result = "Верно! Так держать!"
        } else {
            result = "Ошибка!\n${qa[0]} = ${qa[1]}"
            botRedisRepository.leftPushMsg(userId, word)
        }

        botRedisRepository.deleteAnswer(userId)
        return result
    }
}
