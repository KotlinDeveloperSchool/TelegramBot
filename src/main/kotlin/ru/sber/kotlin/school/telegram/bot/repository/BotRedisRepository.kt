package ru.sber.kotlin.school.telegram.bot.repository

import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.RedisServerCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.util.State

@Service
class BotRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val GAME_STYLE_KEY = "styles"
    private val GAME_DICS_KEY = "dictionary"
    private val STATES_KEY = "states"
    private val ANSWER_KEY = "answer"
    private val PREV_BOT_MSG_KEY = "botMsg"
    private val PREV_MENU_MSG_KEY = "menuMsg"
    private val PREV_USER_MSG_KEY = "userMsg"

    private val gameQueueTemplate = "game_%d"

    fun getQueueName(userId: Long): String = gameQueueTemplate.format(userId)

    fun getQueueSize(userId: Long): Long? = redisTemplate.opsForList().size(getQueueName(userId))

    fun clearQueue(userId: Long) {
        val size = getQueueSize(userId)
        if (size != null)
            redisTemplate.opsForList().trim(getQueueName(userId), size, 0)
    }

    fun leftPushMsg(userId: Long, message: String): String {
        val queue = getQueueName(userId)
        redisTemplate.opsForList().leftPush(queue, message)
        return queue
    }

    fun rightPopMsg(userId: Long): String? =
        redisTemplate.opsForList().rightPop(getQueueName(userId))

    fun popAndPushBack(userId: Long): String? {
        val queue = getQueueName(userId)
        return redisTemplate.opsForList().rightPopAndLeftPush(queue, queue)
    }


    fun getDictionary(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(GAME_DICS_KEY, userId.toString())

    fun putToDictionary(userId: Long, dictionaryId: Long) = redisTemplate.opsForHash<String, String>()
        .put(GAME_DICS_KEY, userId.toString(), dictionaryId.toString())

    fun deleteDictionary(userId: Long) =
        redisTemplate.opsForHash<String, String>().delete(GAME_DICS_KEY, userId.toString())

    fun getState(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(STATES_KEY, userId.toString())

    fun putState(userId: Long, state: State) = redisTemplate.opsForHash<String, String>()
        .put(STATES_KEY, userId.toString(), state.toString())

    fun deleteState(userId: Long) {
        redisTemplate.opsForHash<String, String>().delete(STATES_KEY, userId.toString())
    }

    fun getStyle(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(GAME_STYLE_KEY, userId.toString())

    fun putStyle(userId: Long, style: GameStyle) {
        redisTemplate.opsForHash<String, String>()
            .put(GAME_STYLE_KEY, userId.toString(), style.toString())
    }

    fun deleteStyle(userId: Long) =
        redisTemplate.opsForHash<String, String>().delete(GAME_STYLE_KEY, userId.toString())

    fun getAnswer(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(ANSWER_KEY, userId.toString())

    fun putAnswer(userId: Long, word: String) {
        redisTemplate.opsForHash<String, String>()
            .put(ANSWER_KEY, userId.toString(), word)
    }

    fun deleteAnswer(userId: Long) {
        redisTemplate.opsForHash<String, String>().delete(ANSWER_KEY, userId.toString())
    }

    fun getBotMsg(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(PREV_BOT_MSG_KEY, userId.toString())

    fun putBotMsg(userId: Long, msgId: Int) {
        redisTemplate.opsForHash<String, String>()
            .put(PREV_BOT_MSG_KEY, userId.toString(), msgId.toString())
    }

    fun deleteBotMsg(userId: Long) {
        redisTemplate.opsForHash<String, String>().delete(PREV_BOT_MSG_KEY, userId.toString())
    }

    fun getMenuMsg(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(PREV_MENU_MSG_KEY, userId.toString())

    fun putMenuMsg(userId: Long, msgId: Int) {
        redisTemplate.opsForHash<String, String>()
            .put(PREV_MENU_MSG_KEY, userId.toString(), msgId.toString())
    }

    fun deleteMenuMsg(userId: Long) {
        redisTemplate.opsForHash<String, String>().delete(PREV_MENU_MSG_KEY, userId.toString())
    }

    fun getUserMsg(userId: Long): String? =
        redisTemplate.opsForHash<String, String>().get(PREV_USER_MSG_KEY, userId.toString())

    fun putUserMsg(userId: Long, msgId: Int) {
        redisTemplate.opsForHash<String, String>()
            .put(PREV_USER_MSG_KEY, userId.toString(), msgId.toString())
    }

    fun deleteUserMsg(userId: Long) {
        redisTemplate.opsForHash<String, String>()
            .delete(PREV_USER_MSG_KEY, userId.toString())
    }

    @EventListener(ContextClosedEvent::class)
    fun clearRedisDB() {
        redisTemplate.execute { connection ->
            connection.flushDb(RedisServerCommands.FlushOption.ASYNC)
        }
    }
}
