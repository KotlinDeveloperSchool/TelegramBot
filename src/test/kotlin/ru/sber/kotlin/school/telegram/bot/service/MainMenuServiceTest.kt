package ru.sber.kotlin.school.telegram.bot.service

import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.Collections
import java.util.Optional

internal class MainMenuServiceTest {
    private val update = mockkClass(Update::class)
    private val botRedisRepository = mockkClass(BotRedisRepository::class)
    private var userRepository = mockkClass(UserRepository::class)
    private val menuService = MainMenuService(botRedisRepository, userRepository)

    companion object {
        @JvmStatic
        fun dataForGetAllFromRedis() = listOf(
            Arguments.of(1234, 5678, "", "", "", 2, "", "", ""),
        )

        @JvmStatic
        fun dataForMainMenu() = listOf(
            Arguments.of(
                1234, 5678, Unit, Unit, Unit, Unit, 2, 2, 2,
                User(
                    5678, "superman", "Ivan", "Ivanov",
                    Collections.emptySet<Dictionary>()
                )
            ),
        )
    }

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @ParameterizedTest
    @MethodSource("dataForGetAllFromRedis")
    fun getAllFromRedis(
        chatId: Long, userId: Long, state: String, dic: String, style: String,
        queueSize: Long, botMsg: String, userMsg: String, answer: String
    ) {
        //given
        every { update.message.chat.id } returns chatId
        every { update.message.from.id } returns userId
        every { botRedisRepository.getState(userId) } returns state
        every { botRedisRepository.getDictionary(userId) } returns dic
        every { botRedisRepository.getStyle(userId) } returns style
        every { botRedisRepository.getQueueSize(userId) } returns queueSize
        every { botRedisRepository.getBotMsg(userId) } returns botMsg
        every { botRedisRepository.getUserMsg(userId) } returns userMsg
        every { botRedisRepository.getAnswer(userId) } returns answer
        every { update.hasMessage() } returns true
        every { update.message.chatId } returns chatId

        //when
        val result = menuService.getAllFromRedis(update)

        //then
        verify(exactly = 1) { botRedisRepository.getState(userId) }
        verify(exactly = 1) { botRedisRepository.getDictionary(userId) }
        verify(exactly = 1) { botRedisRepository.getStyle(userId) }
        verify(exactly = 1) { botRedisRepository.getQueueSize(userId) }
        verify(exactly = 1) { botRedisRepository.getBotMsg(userId) }
        verify(exactly = 1) { botRedisRepository.getUserMsg(userId) }
        verify(exactly = 1) { botRedisRepository.getAnswer(userId) }
        assertEquals(
            SendMessage(
                chatId.toString(), "Этап: $state\nСловарь: $dic\nТип игры: $style\nСлов: $queueSize" +
                        "\nОт бота: $botMsg\nОт юзера: $userMsg\nОтвет: $answer"
            ), result
        )
    }

    @ParameterizedTest
    @MethodSource("dataForMainMenu")
    fun mainMenu(
        chatId: Long, userId: Long, clearQueue: Unit, deleteBotMsg: Unit, deleteUserMsg: Unit,
        deleteAnswer: Unit, deleteDictionary: Long, deleteStyle: Long, deleteEditDict: Long, user: User
    ) {
        //given
        every { update.message.chat.id } returns chatId
        every { update.message.from.id } returns userId
        every { update.hasMessage() } returns true
        every { update.message.chatId } returns chatId
        every { botRedisRepository.clearQueue(userId) } returns clearQueue
        every { botRedisRepository.deleteBotMsg(userId) } returns deleteBotMsg
        every { botRedisRepository.deleteUserMsg(userId) } returns deleteUserMsg
        every { botRedisRepository.deleteAnswer(userId) } returns deleteAnswer
        every { botRedisRepository.deleteDictionary(userId) } returns deleteDictionary
        every { botRedisRepository.deleteStyle(userId) } returns deleteStyle
        every { botRedisRepository.deleteEditDict(userId) } returns deleteEditDict
        every { userRepository.findById(any()) } returns Optional.of(user)

        //when
        val result = menuService.createMainMenu(update)

        //then
        verify(exactly = 1) { botRedisRepository.clearQueue(userId) }
        verify(exactly = 1) { botRedisRepository.deleteBotMsg(userId) }
        verify(exactly = 1) { botRedisRepository.deleteUserMsg(userId) }
        verify(exactly = 1) { botRedisRepository.deleteAnswer(userId) }
        verify(exactly = 1) { botRedisRepository.deleteDictionary(userId) }
        verify(exactly = 1) { botRedisRepository.deleteStyle(userId) }
        assertEquals(menuService.createMainMenu(update), result)
    }
}
