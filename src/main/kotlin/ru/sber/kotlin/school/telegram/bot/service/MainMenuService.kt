package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.util.InlineButton

@Service
class MainMenuService(
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
                tgUser.lastName
            )

            userRepository.save(user)
        }
        return "Добро пожаловать, ${tgUser.firstName}!\nДля перехода в главное меню введите команду " +
                "/menu или нажмите на кнопку слева от поля ввода текста.\nЭто действие можно выполнить " +
                "на любом этапе работы с ботом!"
    }

    fun createMainMenu(upd: Update): SendMessage =
        SendMessage.builder()
            .chatId(getChatId(upd).toString())
            .apply {
                val userId = getUser(upd).id
                val hasFavorites = userRepository.findById(userId).get().favorites.isNotEmpty()

                text(mainMenuText(hasFavorites))
                replyMarkup(mainMenuMarkup(hasFavorites))

                clearByUser(userId)
            }
        .build()

    fun updateMainMenu(upd: Update): EditMessageText =
        EditMessageText.builder()
            .chatId(getChatId(upd).toString())
            .apply {
                val userId = getUser(upd).id
                val hasFavorites = userRepository.findById(userId).get().favorites.isNotEmpty()

                text(mainMenuText(hasFavorites))
                replyMarkup(mainMenuMarkup(hasFavorites))

                val menuMsg = botRedisRepository.getMenuMsg(userId)
                messageId(menuMsg!!.toInt())

                clearByUser(userId)
            }
            .build()

    private fun mainMenuText(hasFavorites: Boolean): String {
        val res = StringBuilder("Главное меню")
        if (!hasFavorites)
            res.append("\nДля начала выберите словари для изучения")

        return res.toString()
    }
    private fun mainMenuMarkup(hasFavorites: Boolean): InlineKeyboardMarkup {
        val keyboard = mutableListOf(listOf(InlineButton.DictMenu.getBtn()))
        if (hasFavorites)
            keyboard.add(0, listOf(InlineButton.Training.getBtn()))

        return InlineKeyboardMarkup(keyboard)
    }
    private fun clearByUser(userId: Long) {
        botRedisRepository.clearQueue(userId)
        botRedisRepository.deleteBotMsg(userId)
        botRedisRepository.deleteUserMsg(userId)
        botRedisRepository.deleteAnswer(userId)
        botRedisRepository.deleteDictionary(userId)
        botRedisRepository.deleteStyle(userId)
        botRedisRepository.deleteEditDict(userId)
    }
}
