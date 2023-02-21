package ru.sber.kotlin.school.telegram.bot.game

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import kotlin.test.assertEquals

class GameSelectorTest {
    val ctx = mockk<AnnotationConfigApplicationContext>()
    val botRedisRep = mockk<BotRedisRepository>()
    val gameService = mockk<GameService>()
    val gameSelector = GameSelector(ctx, botRedisRep)

    @Test
    fun getGameServiceGameStyleFreeTypeTest() {
        mockkObject(GameStyle.FreeType)
        every { ctx.getBean(GameStyle.FreeType.toString(), GameService::class.java) } returns gameService

        val result = gameSelector.getGameService(GameStyle.FreeType)
        assertEquals(gameService, result)

        verify { ctx.getBean(GameStyle.FreeType.toString(), GameService::class.java) }
    }

    @Test
    fun getGameServiceGameStyleOneOfFourTest() {
        mockkObject(GameStyle.OneOfFour)
        every { ctx.getBean(GameStyle.OneOfFour.toString(), GameService::class.java) } returns gameService

        val result = gameSelector.getGameService(GameStyle.OneOfFour)
        assertEquals(gameService, result)

        verify { ctx.getBean(GameStyle.OneOfFour.toString(), GameService::class.java) }
    }

    @Test
    fun getGameServiceLongTest() {
        val testStr = "style"
        every { botRedisRep.getStyle(any()) } returns testStr
        every { ctx.getBean(testStr, GameService::class.java) } returns gameService

        val result = gameSelector.getGameService(0)
        assertEquals(gameService, result)

        verify { ctx.getBean(testStr, GameService::class.java) }
    }
}