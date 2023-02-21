package ru.sber.kotlin.school.telegram.bot.game

import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import ru.sber.kotlin.school.telegram.bot.exception.ActionException
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository

interface GameService {
    fun prepare(userId: Long)
    fun getWordsForLearning(userId: Long): String
    fun getTextForRound(userId: Long): String
    fun getKeyboardForRound(userId: Long): ReplyKeyboard

    fun checkAnswer(upd: Update): String

    /**
     * Если ответ хранится в редис-мапе Answer в виде строки в формате:
     * "id#слово#перевод#ещёКакаяТоИнфа", то для проверки ответа можно использовать эту функцию
     */
    fun GameService.defaultCheck(upd: Update, delimiter: String, redisRepo: BotRedisRepository): String {
        val answer = upd.message.text
        val userId = AbilityUtils.getUser(upd).id
        val word = (redisRepo.getAnswer(userId)
            ?: throw ActionException("No question for user $userId"))
        val qa = word.split(delimiter)

        val result: String
        if (answer.equals(qa[2], true)) {
            result = "Верно! Так держать!"
        } else {
            result = "Ошибка!\n${qa[1]} = ${qa[2]}"
            redisRepo.leftPushMsg(userId, word)
        }

        redisRepo.deleteAnswer(userId)
        return result
    }

    /**
     * Дефолтный метод для ознакомления со словарём
     */
    fun GameService.defaultAllWordsFromDic(
        userId: Long,
        delimiter: String,
        redisRepo: BotRedisRepository,
        wordRepo: WordRepository
    ): String {
        val count = redisRepo.getQueueSize(userId) ?: 0
        val idList = ArrayList<Long>()
        for (i in 1..count) {
            idList.add(redisRepo.popAndPushBack(userId)!!.split(delimiter)[0].toLong())
        }

        val result = StringBuilder("Слова на эту тренировку:")
        val words = wordRepo.findAllByIdIn(idList)
        words.forEach { result.append("\n${it.eng}: ${it.rus}") }
        return result.toString()
    }

    fun GameService.defaultEndGame(userId: Long, redisRepo: BotRedisRepository): String {
        redisRepo.deleteState(userId)
        return "Тренировка окончена! Перейдите в главное меню /menu"
    }
}
