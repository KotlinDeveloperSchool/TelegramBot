package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.game.GameSelector
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.util.Button

@Service
class TrainingService(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val botRedisRepository: BotRedisRepository,
    private val gameSelector: GameSelector
) {

    fun getAllFromRedis(upd: Update): SendMessage {
        val userId = getUser(upd).id
        val state = botRedisRepository.getState(userId)
        val dic = botRedisRepository.getDictionary(userId)
        val style = botRedisRepository.getStyle(userId)
        val queueSize = botRedisRepository.getQueueSize(userId)
        val botMsg = botRedisRepository.getBotMsg(userId)
        val userMsg = botRedisRepository.getUserMsg(userId)
        val answer = botRedisRepository.getAnswer(userId)

        return SendMessage(
            getChatId(upd).toString(), "Этап: $state\nСловарь: $dic\nТип игры: $style\nСлов: $queueSize" +
                    "\nОт бота: $botMsg\nОт юзера: $userMsg\nОтвет: $answer"
        )
    }

    fun mainMenu(upd: Update): SendMessage =
        SendMessage(getChatId(upd).toString(), "Выберите пункт меню").apply {
            replyMarkup = InlineKeyboardMarkup().apply {
                clearByUser(getUser(upd).id)
                val trainButton = InlineKeyboardButton("Тренировка")
                    .also {
                        it.switchInlineQueryCurrentChat = "dicts"
                    }
                keyboard = listOf(listOf(trainButton))
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

    fun getFavorites(upd: Update): AnswerInlineQuery {
        val query = upd.inlineQuery

        val user: User = userRepository.findById(1)
            .orElse(User(1, "admin", "admin", "admin"))
        val results = user.favorites
            .map { prepareDictArticle(it) }
            .ifEmpty { listOf(prepareDictArticle(Dictionary(name = "Нет словарей", owner = user))) }

        return AnswerInlineQuery.builder()
            .inlineQueryId(query.id)
            .cacheTime(0)
            .results(results)
            .build()
    }

    private fun prepareDictArticle(dict: Dictionary) = InlineQueryResultArticle.builder()
        .id(dict.id.toString())
        .title(dict.name)
        .inputMessageContent(
            InputTextMessageContent.builder()
                .messageText("Вы выбрали словарь:\n${dict.name}")
                .build()
        )
        .build()

    fun getGameStyles(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text("Теперь выберите режим изучения")
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .oneTimeKeyboard(true)
                .isPersistent(true)
                .keyboard(
                    GameStyle.values()
                        .map { style -> KeyboardRow(listOf(KeyboardButton(style.title()))) }
                ).build()
        )
        .build().also {
            val dictionaryName = upd.message.text.substringAfter('\n')
            val userId = getUser(upd).id
            val dictionaryId = dictionaryRepository.findIdByNameForUser(dictionaryName, userId).get()
            botRedisRepository.putToDictionary(userId, dictionaryId)
        }

    fun prepareGame(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(GameStyle.getDescription(upd))
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .oneTimeKeyboard(true)
                .isPersistent(true)
                .keyboard(
                    listOf(KeyboardRow(listOf(KeyboardButton(Button.GotItLetsGo.text))))
                ).build()
        )
        .build().also {
            val gameStyle = GameStyle.getStyle(upd)
            val userId = getUser(upd).id

            botRedisRepository.putStyle(userId, gameStyle)
            gameSelector.getGameService(gameStyle).prepare(userId)
        }

    fun gameRound(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(getUser(upd).id.let {
            gameSelector.getGameService(it).startRound(it)
        })
        .replyMarkup(ReplyKeyboardRemove(true))
        .build()


    fun gameAnswer(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(
            gameSelector.getGameService(getUser(upd).id).checkAnswer(upd)
        )
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .oneTimeKeyboard(true)
                .isPersistent(true)
                .keyboard(
                    listOf(KeyboardRow(listOf(KeyboardButton(Button.OkNext.text))))
                ).build()
        )
        .build()
}
