package ru.sber.kotlin.school.telegram.bot.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import java.util.*

internal class CreationServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val dictionaryRepository = mockk<DictionaryRepository>()
    private val wordRepository = mockk<WordRepository>()
    private val botRedisRepository = mockk<BotRedisRepository>()
    private val creationService = CreationService(userRepository,dictionaryRepository,wordRepository,botRedisRepository)
    private val update = mockk<Update>()
    private val message = mockk<Message>()

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    companion object{
        @JvmStatic
        fun dataForCreation() = listOf(
            Arguments.of(1234, 5678),
        )

        @JvmStatic
        fun dataForCreateDict() = listOf(
            Arguments.of(1234, 5678, "", User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(Dictionary(0,"Словарь А", Collections.emptyList(),
                    User(5678,"superman","Ivan","Ivanov")))), Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov"))),
        )

        @JvmStatic
        fun dataForAddWord() = listOf(
            Arguments.of(1234, 5678, "hi привет", User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(Dictionary(0,"Словарь А", Collections.emptyList(),
                    User(5678,"superman","Ivan","Ivanov")))), Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov")), Word(0,"привет","hi",Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov"))), listOf<Word>(Word(0,"привет","hi",Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov"))), Word(1,"пока","bye",Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov"))))),
        )

        @JvmStatic
        fun dataForWrongWord() = listOf(
            Arguments.of(1234, 5678, ""),
        )
    }

    @ParameterizedTest
    @MethodSource("dataForCreation")
    fun creation(chatId: Long, userId: Long) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.chatId } returns chatId
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()

        //when
        val result = creationService.creation(update)

        //then
        verify (exactly = 1) { botRedisRepository.getMenuMsg(any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        assertEquals("Введите название словаря\n" +
                "!!! разрешены латиница, кириллица и символ пробела",result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForCreateDict")
    fun createDict(chatId: Long, userId: Long, text: String, user: User, dictionary: Dictionary) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.chatId } returns chatId
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()
        every { update.message.text } returns text
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { dictionaryRepository.save(any()) } returns dictionary
        every { botRedisRepository.putEditDict(any(),any()) } returns Unit

        //when
        val result = creationService.createDict(update)

        //then
        verify (exactly = 1) { botRedisRepository.getMenuMsg(any()) }
        verify (exactly = 1) { userRepository.findById(any()) }
        verify (exactly = 1) { dictionaryRepository.save(any()) }
        verify (exactly = 1) { botRedisRepository.putEditDict(any(),any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        assertEquals("Создан словарь '${dictionary.name}'\n" +
                " Теперь введите слово и его перевод, порядок слов неважен\n" +
                "!!! разрешены латиница, кириллица и символ пробела",result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForAddWord")
    fun addWord(chatId: Long, userId: Long, text: String, user: User, dictionary: Dictionary, word: Word, words: List<Word>) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.chatId } returns chatId
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()
        every { update.message.text } returns text
        every { botRedisRepository.getEditDict(any()) } returns userId.toString()
        every { dictionaryRepository.findById(any()) } returns Optional.of(dictionary)
        every { wordRepository.save(any()) } returns word
        every { wordRepository.findAllByDictionaryId(any()) } returns words

        //when
        val result = creationService.addWord(update)

        //then
        verify (exactly = 1) { botRedisRepository.getMenuMsg(any()) }
        verify (exactly = 1) { botRedisRepository.getEditDict(any()) }
        verify (exactly = 1) { dictionaryRepository.findById(any()) }
        verify (exactly = 1) { wordRepository.save(any()) }
        verify (exactly = 1) { wordRepository.findAllByDictionaryId(any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        assertEquals("Редактируемый словарь '${dictionary.name}':\n" +
                "1. ${words.get(0).eng} - ${words.get(0).rus}\n" +
                "2. ${words.get(1).eng} - ${words.get(1).rus}",result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForWrongWord")
    fun wrongWord(chatId: Long, userId: Long, text: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.chatId } returns chatId
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()
        every { update.message.text } returns text

        //when
        val result = creationService.wrongWord(update)

        //then
        verify (exactly = 1) { botRedisRepository.getMenuMsg(any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        assertEquals("Слово '$text' не соответствует правилам ввода. Вот пара примеров:\n" +
                "1) polar bear белый медведь\n" +
                "2) белый медведь polar bear",result.text)
    }
}