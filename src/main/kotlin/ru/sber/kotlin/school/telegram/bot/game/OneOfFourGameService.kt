package ru.sber.kotlin.school.telegram.bot.game

import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.exception.ActionException
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import ru.sber.kotlin.school.telegram.bot.util.Button
import kotlin.random.Random

@Component("OneOfFour")
class OneOfFourGameService(
    private val wordRepository: WordRepository,
    private val botRedisRepository: BotRedisRepository
) : GameService {
    private val DELIMITER = "#"
    private val TEMPLATE = "%d$DELIMITER%s$DELIMITER%s$DELIMITER%s"

    override fun prepare(userId: Long) {
        val dictionaryId = (botRedisRepository.getDictionary(userId)
            ?: throw ActionException("No chosen dictionary by user $userId")).toLong()
        val allWords = wordRepository.findAllByDictionaryId(dictionaryId)
        val wordsToLearn = allWords.getRandomWords(5)

        wordsToLearn.forEach {
            val others = wordsToLearn.getThreeOtherExcept(it)
            if (Random.nextInt(2) == 0)
                botRedisRepository.leftPushMsg(userId, TEMPLATE.format(
                    it.id,
                    it.rus,
                    it.eng,
                    others.joinToString(DELIMITER) { other -> other.eng }
                ))
            else
                botRedisRepository.leftPushMsg(userId, TEMPLATE.format(
                    it.id,
                    it.eng,
                    it.rus,
                    others.joinToString(DELIMITER) { other -> other.rus }
                ))
        }
    }

    override fun getWordsForLearning(userId: Long): String =
        defaultAllWordsFromDic(userId, DELIMITER, botRedisRepository, wordRepository)

    private fun List<Word>.getRandomWords(count: Int): MutableSet<Word> {
        val result = HashSet<Word>()
        while (result.size < count) {
            val i = Random.nextInt(this.size)
            result.add(this[i])
        }
        return result
    }

    private fun MutableSet<Word>.getThreeOtherExcept(word: Word): MutableSet<Word> {
        val result = HashSet<Word>()
        this.filter { it != word }
            .toCollection(result)
        while (result.size > 3) {
            result.remove(result.first())
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

    override fun getKeyboardForRound(userId: Long): ReplyKeyboard {
        val word = botRedisRepository.getAnswer(userId)
        return if (word != null) {
            val buttons = word.split(DELIMITER)
                .filterIndexed { i, _ -> i > 1 }
                .map { KeyboardButton(it) }

            ReplyKeyboardMarkup.builder()
                .isPersistent(true)
                .keyboard(
                    listOf(
                        KeyboardRow(buttons.filterIndexed { i, _ -> i % 2 == 0 }),
                        KeyboardRow(buttons.filterIndexed { i, _ -> i % 2 == 1})
                    )
                ).build()
        } else ReplyKeyboardRemove(true)
    }

    override fun checkAnswer(upd: Update): String =
        defaultCheck(upd, DELIMITER, botRedisRepository)

}
