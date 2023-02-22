package ru.sber.kotlin.school.telegram.bot.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.*

internal class DictionaryServiceTest {
    private val dictionaryRepository = mockk<DictionaryRepository>()
    private val userRepository = mockk<UserRepository>()
    private val dictionaryMenuService = mockk<DictionaryMenuService>()
    private val dictionaryService = DictionaryService(dictionaryRepository,userRepository,dictionaryMenuService)
    private val update = mockk<Update>()
    private val message = mockk<Message>()
    private val editMessageText = mockk<EditMessageText>()

    companion object{
        @JvmStatic
        fun dataForAddDictionaryToFavorites() = listOf(
            Arguments.of(5678, User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(
                    Dictionary(0,"Словарь А", Collections.emptyList(),
                        User(5678,"superman","Ivan","Ivanov")))), Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov")
            ), ""),
        )

        @JvmStatic
        fun dataForDeleteDictionaryFromFavorites() = listOf(
            Arguments.of(5678, User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(
                    Dictionary(0,"Словарь А", Collections.emptyList(),
                        User(5678,"superman","Ivan","Ivanov")))), Dictionary(0,"Словарь А", Collections.emptyList(),
                User(5678,"superman","Ivan","Ivanov")
            ), ""),
        )
    }

    @ParameterizedTest
    @MethodSource("dataForAddDictionaryToFavorites")
    fun addDictionaryToFavorites(userId: Long, user: User, dictionary: Dictionary, text: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.text } returns text
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { dictionaryRepository.findByNameForUser(any(),any()) } returns Optional.of(dictionary)
        every { userRepository.save(any()) } returns user
        every { dictionaryMenuService.getDictMenu(update) } returns editMessageText

        //when
        dictionaryService.addDictionaryToFavorites(update)

        //then
        verify (exactly = 1) {
            userRepository.findById(any())
            dictionaryRepository.findByNameForUser(any(),any())
            userRepository.save(any())
            dictionaryMenuService.getDictMenu(update)
        }
    }

    @ParameterizedTest
    @MethodSource("dataForDeleteDictionaryFromFavorites")
    fun deleteDictionaryFromFavorites(userId: Long, user: User, dictionary: Dictionary, text: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.from.id } returns userId
        every { update.message.text } returns text
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { dictionaryRepository.findByNameForUser(any(),any()) } returns Optional.of(dictionary)
        every { userRepository.save(any()) } returns user
        every { dictionaryMenuService.getDictMenu(update) } returns editMessageText

        //when
        dictionaryService.deleteDictionaryFromFavorites(update)

        //then
        verify (exactly = 1) {
            userRepository.findById(any())
            dictionaryRepository.findByNameForUser(any(),any())
            userRepository.save(any())
            dictionaryMenuService.getDictMenu(update)
        }
    }
}