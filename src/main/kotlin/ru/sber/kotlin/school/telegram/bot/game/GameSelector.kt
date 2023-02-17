package ru.sber.kotlin.school.telegram.bot.game

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository

@Component
class GameSelector(
    private val ctx: AnnotationConfigApplicationContext,
    private val botRedisRepository: BotRedisRepository
) {
    fun getGameService(style: GameStyle): GameService =
        ctx.getBean(style.toString(), GameService::class.java)

    fun getGameService(userId: Long): GameService {
        val style = botRedisRepository.getStyle(userId)!!
        return ctx.getBean(style, GameService::class.java)
    }
}
