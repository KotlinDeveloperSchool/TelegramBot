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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.*

class DictionaryMenuServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val dictionaryRepository = mockk<DictionaryRepository>()
    private val botRedisRepository = mockk<BotRedisRepository>()
    private val dictionaryMenuService = DictionaryMenuService(userRepository,dictionaryRepository,botRedisRepository)
    private val update = mockk<Update>()
    private val message = mockk<Message>()
    private val query = mockkClass(InlineQuery::class)

    companion object{
        @JvmStatic
        fun dataForGetDictMenu() = listOf(
            Arguments.of(1234, 5678, User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(
                    Dictionary(0,"Словарь А", Collections.emptyList(),
                        User(5678,"superman","Ivan","Ivanov")
                    )
                ))),
        )

        @JvmStatic
        fun dataForAddToFavMenu() = listOf(
            Arguments.of(5678, listOf(Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov")), Dictionary(0,"Словарь Б", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov"))), "4"),
        )

        @JvmStatic
        fun dataForDeleteFromFavMenu() = listOf(
            Arguments.of(5678, User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(
                    Dictionary(0,"Словарь А", Collections.emptyList(),
                        User(5678,"superman","Ivan","Ivanov")))), "4"),
        )
    }

    @ParameterizedTest
    @MethodSource("dataForGetDictMenu")
    fun getDictMenu(chatId: Long, userId: Long, user: User) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.chatId } returns chatId
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()
        every { userRepository.findById(any()) } returns Optional.of(user)

        //when
        val result = dictionaryMenuService.getDictMenu(update)

        //then
        verify (exactly = 1) { botRedisRepository.getMenuMsg(any()) }
        verify (exactly = 1) { userRepository.findById(any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        var count = 1
        user.favorites.forEach {
            assertEquals("Список словарей у Вас на изучении: \n" +
                    "$count. ${it.name}",result.text)
            count += 1
        }
    }

    @ParameterizedTest
    @MethodSource("dataForAddToFavMenu")
    fun addToFavMenu(userId: Long, dictionaries: List<Dictionary>, queryId: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { dictionaryRepository.findNotFavoritesForUser(any()) } returns dictionaries
        every { update.inlineQuery } returns query
        every { query.id } returns queryId

        //when
        val result = dictionaryMenuService.addToFavMenu(update)

        //then
        verify (exactly = 1) { dictionaryRepository.findNotFavoritesForUser(any()) }
        assertEquals(queryId, result.inlineQueryId)
    }

    @ParameterizedTest
    @MethodSource("dataForDeleteFromFavMenu")
    fun deleteFromFavMenu(userId: Long, user: User, queryId: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { update.inlineQuery } returns query
        every { query.id } returns queryId

        //when
        val result = dictionaryMenuService.deleteFromFavMenu(update)

        //then
        verify (exactly = 1) { userRepository.findById(any()) }
        assertEquals(queryId, result.inlineQueryId)
    }
}