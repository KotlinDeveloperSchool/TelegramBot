package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import java.util.Collections

@Service
class MenuService(
    private val botRedisRepository: BotRedisRepository,
    private val userRepository: UserRepository
) {
    fun getAllFromRedis(upd: Update): SendMessage {
        val userId = AbilityUtils.getUser(upd).id
        val state = botRedisRepository.getState(userId)
        val dic = botRedisRepository.getDictionary(userId)
        val style = botRedisRepository.getStyle(userId)
        val queueSize = botRedisRepository.getQueueSize(userId)
        val botMsg = botRedisRepository.getBotMsg(userId)
        val userMsg = botRedisRepository.getUserMsg(userId)
        val answer = botRedisRepository.getAnswer(userId)

        return SendMessage(
            AbilityUtils.getChatId(upd).toString(), "Этап: $state\nСловарь: $dic\nТип игры: $style\nСлов: $queueSize" +
                    "\nОт бота: $botMsg\nОт юзера: $userMsg\nОтвет: $answer"
        )
    }

    fun startByUser(upd: Update): SendMessage = SendMessage.builder()
        .chatId(AbilityUtils.getChatId(upd).toString())
        .text(registerUser(upd))
        .replyMarkup(ReplyKeyboardRemove(true))
        .build()

    private fun registerUser(upd: Update): String {
        val tgUser = AbilityUtils.getUser(upd)
        if (!userRepository.existsById(tgUser.id)) {
            val user = User(
                tgUser.id,
                tgUser.userName,
                tgUser.firstName,
                tgUser.lastName,
                Collections.emptyList()
            )

            userRepository.save(user)
        }
        return "Добро пожаловать, ${tgUser.firstName}!\nДля перехода в главное меню введите команду " +
                "/menu или нажмите на кнопку слева от поля ввода текста.\nЭто действие можно выполнить " +
                "на любом этапе работы с ботом!"
    }

    fun mainMenu(upd: Update): SendMessage =
        SendMessage(AbilityUtils.getChatId(upd).toString(), "Выберите пункт меню").apply {
            replyMarkup = InlineKeyboardMarkup().apply {
                clearByUser(AbilityUtils.getUser(upd).id)
                val trainButton = InlineKeyboardButton("Тренировка")
                    .also {
                        it.switchInlineQueryCurrentChat = "dicts"
                    }
                val dictButton = InlineKeyboardButton("Словари")
                    .also {
                        it.callbackData = "dictMenu"
                    }
                keyboard = listOf(listOf(trainButton, dictButton))
            }
        }

    private fun clearByUser(userId: Long) {
        botRedisRepository.clearQueue(userId)
        botRedisRepository.deleteBotMsg(userId)
        botRedisRepository.deleteUserMsg(userId)
        botRedisRepository.deleteAnswer(userId)
        botRedisRepository.deleteDictionary(userId)
        botRedisRepository.deleteStyle(userId)
    }
}
