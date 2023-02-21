package ru.sber.kotlin.school.telegram.bot.game

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.exception.ActionException
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import kotlin.test.assertEquals
import org.telegram.telegrambots.meta.api.objects.User as UserBots

class FreeTypeGameServiceTest {
    val wordRep = mockk<WordRepository>()
    val botRedisRep = mockk<BotRedisRepository>()
    val freeTypeGameService = FreeTypeGameService(wordRep, botRedisRep)
    val user = mockk<UserBots>()
    val update = mockk<Update>()
    val dic = Dictionary(0, "Test", mutableListOf(),
        User(0, "test", "test", "test", mutableSetOf()))
    val wordsList = mutableListOf(
        Word(0, "rus", "eng", dic),
        Word(1, "rus1", "eng1", dic),
        Word(2, "rus2", "eng2", dic),
        Word(3, "rus3", "eng3", dic))

    @AfterEach
    fun unMockk() {
        unmockkAll()
    }

    @Test
    fun prepareTest() {
        every { botRedisRep.getDictionary(any()) } returns "0"
        every { wordRep.findAllByDictionaryId(any()) } returns wordsList
        every { botRedisRep.leftPushMsg(any(), any()) } returns ""
        freeTypeGameService.prepare(0)

        verify(exactly = 4) {
            botRedisRep.leftPushMsg(any(), any())
        }
    }

    @Test
    fun prepareExceptionTest() {
        every { botRedisRep.getDictionary(any()) } returns null

        assertThrows<ActionException> {
            freeTypeGameService.prepare(0)
        }

        verify { botRedisRep.getDictionary(any()) }
    }

    @Test
    fun getWordsForLearningTest() {
        every { botRedisRep.getQueueSize(any()) } returns 4
        every { botRedisRep.popAndPushBack(any()) } returnsMany listOf("1", "2", "3","4")
        every { wordRep.findAllByIdIn(any()) } returns wordsList

        val result = freeTypeGameService.getWordsForLearning(0)

        val expected = """
            Слова на эту тренировку:
            eng: rus
            eng1: rus1
            eng2: rus2
            eng3: rus3
        """.trimIndent()

        assertEquals(expected, result)

        verify {
            botRedisRep.getQueueSize(any())
            botRedisRep.popAndPushBack(any())
            wordRep.findAllByIdIn(any())
        }
    }

    @Test
    fun getTextForRoundTest1() {
        every { botRedisRep.putAnswer(any(), any()) } just Runs

        every { botRedisRep.rightPopMsg(any()) } returns "word#stub"
        val result = freeTypeGameService.getTextForRound(0)
        assertEquals("stub", result)

        verify { botRedisRep.putAnswer(any(), any()) }
    }

    @Test
    fun getTextForRoundTest2() {
        every { botRedisRep.rightPopMsg(any()) } returns null
        every { botRedisRep.deleteState(any()) } just Runs

        val result = freeTypeGameService.getTextForRound(0)

        assertEquals(
            "Тренировка окончена! Но вы можете начать сначала...",
            result
        )

        verify {
            botRedisRep.rightPopMsg(any())
        }
    }

    @Test
    fun checkAnswerCorrectTest() {
        mockkStatic(AbilityUtils::class)
        every { AbilityUtils.getUser(any()) } returns user
        every { user.id } returns 0
        every { update.message.text } returns "rus"
        every { botRedisRep.getAnswer(any()) } returns "id#eng#rus"
        every { botRedisRep.deleteAnswer(any()) } just Runs

        val result = freeTypeGameService.checkAnswer(update)

        assertEquals("Верно! Так держать!", result)
    }

    @Test
    fun checkAnswerErrorTest() {
        mockkStatic(AbilityUtils::class)
        every { AbilityUtils.getUser(any()) } returns user
        every { user.id } returns 0
        every { update.message.text } returns "rus1"
        every { botRedisRep.getAnswer(any()) } returns "id#eng#rus"
        every { botRedisRep.deleteAnswer(any()) } just Runs
        every { botRedisRep.leftPushMsg(any(), any()) } returns ""

        val result = freeTypeGameService.checkAnswer(update)
        val expected = """
            Ошибка!
            eng = rus
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun checkDefaultCheckException() {
        mockkStatic(AbilityUtils::class)
        every { update.message.text } returns "exc"
        every { AbilityUtils.getUser(any()) } returns user
        every { user.id } returns 0
        every { botRedisRep.getAnswer(any()) } returns null

        assertThrows<ActionException> { freeTypeGameService.checkAnswer(update) }
    }
}