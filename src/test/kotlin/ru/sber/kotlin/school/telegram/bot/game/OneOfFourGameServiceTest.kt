package ru.sber.kotlin.school.telegram.bot.game

import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.sber.kotlin.school.telegram.bot.exception.ActionException
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import org.telegram.telegrambots.meta.api.objects.User as UserBots
import kotlin.test.assertEquals

class OneOfFourGameServiceTest {

    val wordRep = mockk<WordRepository>()
    val botRedisRep = mockk<BotRedisRepository>()
    val user = mockk<UserBots>()
    val update = mockk<Update>()
    val oneOfFourGameService = OneOfFourGameService(wordRep, botRedisRep)
    val dic = Dictionary(0, "Test", mutableListOf(),
        User(0, "test", "test", "test", mutableSetOf())
    )
    val wordsList = mutableListOf(
        Word(0, "rus", "eng", dic),
        Word(1, "rus1", "eng1", dic),
        Word(2, "rus2", "eng2", dic),
        Word(3, "rus3", "eng3", dic),
        Word(4, "rus4", "eng4", dic)
    )

    @Test
    fun prepareTest() {
        every { botRedisRep.getDictionary(any()) } returns "0"
        every { wordRep.findAllByDictionaryId(any()) } returns wordsList
        every { botRedisRep.leftPushMsg(any(), any()) } returns ""
        oneOfFourGameService.prepare(0)

        verify(exactly = 5) { botRedisRep.leftPushMsg(any(), any()) }
    }

    @Test
    fun prepareExceptionTest() {
        every { botRedisRep.getDictionary(any()) } returns null
        assertThrows<ActionException> {
            oneOfFourGameService.prepare(0)
        }
        verify { botRedisRep.getDictionary(any()) }
    }

    @Test
    fun getWordsForLearningTest() {
        every { botRedisRep.getQueueSize(any()) } returns 4
        every { botRedisRep.popAndPushBack(any()) } returnsMany listOf("1", "2", "3","4")
        every { wordRep.findAllByIdIn(any()) } returns wordsList

        val result = oneOfFourGameService.getWordsForLearning(0)

        val expected = """
            Слова на эту тренировку:
            eng: rus
            eng1: rus1
            eng2: rus2
            eng3: rus3
            eng4: rus4
        """.trimIndent()

        assertEquals(expected, result)

        verify {
            botRedisRep.getQueueSize(any())
            botRedisRep.popAndPushBack(any())
            wordRep.findAllByIdIn(any())
        }
    }

    @Test
    fun getTextForRoundTest() {
        every { botRedisRep.rightPopMsg(any()) } returns "1#stub"
        every { botRedisRep.putAnswer(any(), any()) } just Runs

        val result = oneOfFourGameService.getTextForRound(0)

        assertEquals("stub", result)
    }

    @Test
    fun getTextForRoundTestOfNull() {
        every { botRedisRep.rightPopMsg(any()) } returns null
        every { botRedisRep.deleteState(any()) } just Runs

        val result = oneOfFourGameService.getTextForRound(0)
        val expected = "Тренировка окончена! Но вы можете начать сначала..."
        assertEquals(expected, result)
    }

    @Test
    fun getKeyboardForRoundTest() {
        mockkStatic(ReplyKeyboardMarkup::class)
        every { botRedisRep.getAnswer(any()) } returns "1#stub"

        val result = oneOfFourGameService.getKeyboardForRound(0) as ReplyKeyboardMarkup

        assertEquals(2, result.keyboard.size)
    }

    @Test
    fun getKeyboardForRoundTestOfNull() {
        every { botRedisRep.getAnswer(0) } returns null

        val result = oneOfFourGameService.getKeyboardForRound(0) as ReplyKeyboardRemove

        assertEquals(true, result.removeKeyboard)
    }

    @Test
    fun checkAnswerCorrectTest() {
        mockkStatic(AbilityUtils::class)
        every { AbilityUtils.getUser(any()) } returns user
        every { user.id } returns 0
        every { update.message.text } returns "rus"
        every { botRedisRep.getAnswer(any()) } returns "id#eng#rus"
        every { botRedisRep.deleteAnswer(any()) } just Runs

        val result = oneOfFourGameService.checkAnswer(update)

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

        val result = oneOfFourGameService.checkAnswer(update)
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

        assertThrows<ActionException> { oneOfFourGameService.checkAnswer(update) }
    }
}