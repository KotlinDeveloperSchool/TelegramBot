package ru.sber.kotlin.school.telegram.bot.service

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import ru.sber.kotlin.school.telegram.bot.game.GameSelector
import ru.sber.kotlin.school.telegram.bot.game.GameService
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.*

internal class TrainingServiceTest {
    private var userRepository = mockkClass(UserRepository::class)
    private val dictionaryRepository = mockkClass(DictionaryRepository::class)
    private val botRedisRepository = mockkClass(BotRedisRepository::class)
    private val gameSelector = mockkClass(GameSelector::class)
    private val update = mockkClass(Update::class)
    private val trainingService = TrainingService(userRepository,dictionaryRepository,botRedisRepository,gameSelector)
    private val gameService = mockkClass(GameService::class)
    private val message = mockkClass(Message::class)
    private val query = mockkClass(InlineQuery::class)

    companion object {

        @JvmStatic
        fun dataForGetFavorites() = listOf(
            Arguments.of("4",User(2,"superman","Ivan","Ivanov",
                Collections.emptySet<Dictionary>()),5678),
            Arguments.of("4",User(2,"superman","Ivan","Ivanov",
                mutableSetOf(Dictionary(0,"Словарь А", Collections.emptyList(),
                    User(2,"superman","Ivan","Ivanov")))),5678),
        )

        @JvmStatic
        fun dataForGetGameStyles() = listOf(
            Arguments.of(1234, 5678, "Время", 2),
        )

        @JvmStatic
        fun dataForPrepareGame() = listOf(
            Arguments.of(1234, 5678, "На свободный ввод", GameStyle.FreeType),
        )

        @JvmStatic
        fun dataForShowWords() = listOf(
            Arguments.of(1234, 5678, "привет=hello"),
        )

        @JvmStatic
        fun dataForGameRound() = listOf(
            Arguments.of(1234, 5678, ""),
        )

        @JvmStatic
        fun dataForGameAnswer() = listOf(
            Arguments.of(1234, 5678, ""),
        )
    }

    @ParameterizedTest
    @MethodSource("dataForGetFavorites")
    fun getFavorites(queryId: String, user: User, userId: Long) {
        //given
        every { update.hasMessage() } returns true
        every { update.inlineQuery } returns query
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { query.id } returns queryId

        //when
        val result = trainingService.getFavorites(update)

        //then
        verify (exactly = 1) { userRepository.findById(any()) }
        assertEquals(queryId,result.inlineQueryId)
    }

    @ParameterizedTest
    @MethodSource("dataForGetGameStyles")
    fun getGameStyles(chatId: Long, userId: Long, dictionaryName: String, dictionaryId: Long){
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.text } returns dictionaryName
        every { update.message.from.id } returns userId
        every { dictionaryRepository.findByNameForUser(any(), any()).get().id } returns dictionaryId
        every { botRedisRepository.putDictionary(any(), any()) } returns Unit

        //when
        val result = trainingService.getGameStyles(update)

        //then
        verify (exactly = 1) { dictionaryRepository.findByNameForUser(any(), any()).get() }
        verify (exactly = 1) { botRedisRepository.putDictionary(any(), any()) }
        assertEquals(chatId, result.chatId.toLong())
        assertEquals("Теперь выберите режим изучения", result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForPrepareGame")
    fun prepareGame(chatId: Long, userId: Long, text: String, gStyle: GameStyle) {
        //given
        val gameStyle = mockkClass(gStyle::class)
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.text } returns text
        every { gameStyle.title() } returns text
        every { update.message.from.id } returns userId
        every { botRedisRepository.putStyle(any(),any()) } returns Unit
        every { gameSelector.getGameService(gStyle).prepare(any()) } returns Unit

        //when
        val result = trainingService.prepareGame(update)

        //then
        verify (exactly = 1) { botRedisRepository.putStyle(any(),any()) }
        verify (exactly = 1) { gameSelector.getGameService(gStyle).prepare(any()) }
        assertEquals(chatId, result.chatId.toLong())
        assertEquals("На экране будет представлено слово, Вам необходимо " +
                "ввести и отправить его перевод", result.text)

    }

    @ParameterizedTest
    @MethodSource("dataForShowWords")
    fun showWords(chatId: Long, userId: Long, words: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.from.id } returns userId
        every { gameSelector.getGameService(userId) } returns gameService
        every { gameService.getWordsForLearning(any()) } returns words

        //when
        val result = trainingService.showWords(update)

        //then
        verify (exactly = 1) { gameSelector.getGameService(userId) }
        verify (exactly = 1) { gameService.getWordsForLearning(any()) }
        assertEquals(chatId, result.chatId.toLong())
        assertEquals(words, result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForGameRound")
    fun gameRound(chatId: Long, userId: Long, text: String) {
        //given
        val repleKeyboard = mockkClass(ReplyKeyboard::class)
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.from.id } returns userId
        every { gameSelector.getGameService(userId) } returns gameService
        every { gameService.getTextForRound(any()) } returns text
        every { gameService.getKeyboardForRound(any()) } returns repleKeyboard

        //when
        val result = trainingService.gameRound(update)

        //then
        verify (exactly = 2) { gameSelector.getGameService(userId) }
        verify (exactly = 1) { gameService.getTextForRound(any()) }
        verify (exactly = 1) { gameService.getKeyboardForRound(any()) }
        assertEquals(chatId, result.chatId.toLong())
        assertEquals(text, result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForGameAnswer")
    fun gameAnswer(chatId: Long, userId: Long, text: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.from.id } returns userId
        every { gameSelector.getGameService(userId) } returns gameService
        every { gameService.checkAnswer(update) } returns text

        //when
        val result = trainingService.gameAnswer(update)

        //then
        verify (exactly = 1) { gameSelector.getGameService(userId) }
        verify (exactly = 1) { gameService.checkAnswer(update) }
        assertEquals(chatId, result.chatId.toLong())
        assertEquals(text, result.text)
    }
}