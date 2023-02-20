package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils
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
import java.util.Collections

@Service
class TrainingService(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val botRedisRepository: BotRedisRepository,
    private val gameSelector: GameSelector
) {

    fun getFavorites(upd: Update): AnswerInlineQuery {
        val query = upd.inlineQuery

        val user: User = userRepository.findById(getUser(upd).id)
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
                .isPersistent(true)
                .keyboard(
                    GameStyle.values()
                        .map { style -> KeyboardRow(listOf(KeyboardButton(style.title()))) }
                ).build()
        )
        .build().also {
            val dictionaryName = upd.message.text.substringAfter('\n')
            val userId = getUser(upd).id
            val dictionaryId = dictionaryRepository.findByNameForUser(dictionaryName, userId).get().id
            botRedisRepository.putToDictionary(userId, dictionaryId)
        }

    fun prepareGame(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(GameStyle.getDescription(upd))
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .isPersistent(true)
                .keyboard(
                    listOf(
                        KeyboardRow(listOf(Button.GotItLetsGo.getBtn())),
                        KeyboardRow(listOf(Button.ShowWordsFromDic.getBtn()))
                    )
                ).build()
        )
        .build().also {
            val gameStyle = GameStyle.getStyle(upd)
            val userId = getUser(upd).id

            botRedisRepository.putStyle(userId, gameStyle)
            gameSelector.getGameService(gameStyle).prepare(userId)
        }

    fun showWords(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(getUser(upd).id.let {
            gameSelector.getGameService(it).getWordsForLearning(it)
        })
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .isPersistent(true)
                .keyboard(
                    listOf(
                        KeyboardRow(listOf(Button.GotItLetsGo.getBtn()))
                    )
                ).build()
        )
        .build()


    fun gameRound(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(getUser(upd).id.let {
            gameSelector.getGameService(it).getTextForRound(it)
        })
        .replyMarkup(getUser(upd).id.let {
            gameSelector.getGameService(it).getKeyboardForRound(it)
        })
        .build()


    fun gameAnswer(upd: Update): SendMessage = SendMessage.builder()
        .chatId(getChatId(upd))
        .text(
            gameSelector.getGameService(getUser(upd).id).checkAnswer(upd)
        )
        .replyMarkup(
            ReplyKeyboardMarkup.builder()
                .isPersistent(true)
                .keyboard(
                    listOf(KeyboardRow(listOf(Button.OkNext.getBtn())))
                ).build()
        )
        .build()
}
