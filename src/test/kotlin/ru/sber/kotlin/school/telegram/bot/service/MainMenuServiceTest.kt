package ru.sber.kotlin.school.telegram.bot.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
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
    private val message = mockk<Message>()

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

        @JvmStatic
        fun dataForStartByUser() = listOf(
            Arguments.of(1234, 5678, true, ""),
        )

        @JvmStatic
        fun dataForUpdateMainMenu() = listOf(
            Arguments.of(1234, 5678, User(5678,"superman","Ivan","Ivanov",
                mutableSetOf(
                    Dictionary(0,"Словарь А", Collections.emptyList(),
                        User(5678,"superman","Ivan","Ivanov")
                    )
                )), 1, 1, 1),
        )
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
        every { botRedisRepository.getState(any()) } returns state
        every { botRedisRepository.getDictionary(any()) } returns dic
        every { botRedisRepository.getStyle(any()) } returns style
        every { botRedisRepository.getQueueSize(any()) } returns queueSize
        every { botRedisRepository.getBotMsg(any()) } returns botMsg
        every { botRedisRepository.getUserMsg(any()) } returns userMsg
        every { botRedisRepository.getAnswer(any()) } returns answer
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
        every { botRedisRepository.clearQueue(any()) } returns clearQueue
        every { botRedisRepository.deleteBotMsg(any()) } returns deleteBotMsg
        every { botRedisRepository.deleteUserMsg(any()) } returns deleteUserMsg
        every { botRedisRepository.deleteAnswer(any()) } returns deleteAnswer
        every { botRedisRepository.deleteDictionary(any()) } returns deleteDictionary
        every { botRedisRepository.deleteStyle(any()) } returns deleteStyle
        every { botRedisRepository.deleteEditDict(any()) } returns deleteEditDict
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
    }

    @ParameterizedTest
    @MethodSource("dataForStartByUser")
    fun startByUser(chatId: Long, userId: Long, existsById: Boolean, firstName: String) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.from.id } returns userId
        every { userRepository.existsById(any()) } returns existsById
        every { update.message.from.firstName } returns firstName

        //when
        val result = menuService.startByUser(update)

        //then
        verify (exactly = 1) { userRepository.existsById(any()) }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals("Добро пожаловать, !\n" +
                "Для перехода в главное меню введите команду /menu или нажмите на кнопку слева от поля ввода текста.\n" +
                "Это действие можно выполнить на любом этапе работы с ботом!",result.text)
    }

    @ParameterizedTest
    @MethodSource("dataForUpdateMainMenu")
    fun updateMainMenu(chatId: Long, userId: Long, user: User, deleteDictionary: Long,
                       deleteStyle: Long, deleteEditDict: Long) {
        //given
        every { update.hasMessage() } returns true
        every { update.message } returns message
        every { update.message.chatId } returns chatId
        every { update.message.from.id } returns userId
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { botRedisRepository.getMenuMsg(any()) } returns userId.toString()
        every { botRedisRepository.clearQueue(any()) } returns Unit
        every { botRedisRepository.deleteBotMsg(any()) } returns Unit
        every { botRedisRepository.deleteUserMsg(any()) } returns Unit
        every { botRedisRepository.deleteAnswer(any()) } returns Unit
        every { botRedisRepository.deleteDictionary(any()) } returns deleteDictionary
        every { botRedisRepository.deleteStyle(any()) } returns deleteStyle
        every { botRedisRepository.deleteEditDict(any()) } returns deleteEditDict

        //when
        val result = menuService.updateMainMenu(update)

        //then
        verify (exactly = 1) {
            userRepository.findById(any())
            botRedisRepository.getMenuMsg(any())
            botRedisRepository.clearQueue(any())
            botRedisRepository.deleteBotMsg(any())
            botRedisRepository.deleteUserMsg(any())
            botRedisRepository.deleteAnswer(any())
            botRedisRepository.deleteDictionary(any())
            botRedisRepository.deleteStyle(any())
            botRedisRepository.deleteEditDict(any())
        }
        assertEquals(chatId.toString(),result.chatId)
        assertEquals(userId.toInt(),result.messageId)
        assertEquals("Главное меню",result.text)
    }
}
