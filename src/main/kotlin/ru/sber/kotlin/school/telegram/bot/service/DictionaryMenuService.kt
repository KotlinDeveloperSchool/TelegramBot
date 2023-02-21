package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.util.InlineButton

@Service
class DictionaryMenuService(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val botRedisRepository: BotRedisRepository
) {

    fun getDictMenu(upd: Update): EditMessageText {
        val chatId = getChatId(upd).toString()
        val userId = getUser(upd).id

        val prevMsg = botRedisRepository.getMenuMsg(userId)

        val messageBuilder = EditMessageText.builder()
            .chatId(chatId)
            .messageId(prevMsg!!.toInt())

        val buttons = mutableListOf(
            InlineButton.CreateNewDictionary,
            InlineButton.AddFromReady,
            InlineButton.MainMenu
        )

        val favorites = getFavorites(userId)

        if (favorites.isNotEmpty()) {
            buttons.add(2, InlineButton.DeleteFavDictionary)
            messageBuilder.text("Список словарей у Вас на изучении: ${getFavoriteText(favorites)}")
        } else {
            messageBuilder.text("У вас нет выбранных словарей для изучения. Выберите из предложенных или создайте свой")
        }

        messageBuilder.replyMarkup(prepareInlineMarkup(buttons))

        return messageBuilder.build()
    }

    private fun getFavorites(userId: Long) =
        userRepository.findById(userId).get().favorites

    private fun getFavoriteText(favorites: Collection<Dictionary>): String {
        var result = ""
        favorites.forEachIndexed { i, favDictionary ->
            result += "\n${i + 1}. ${favDictionary.name}"
        }

        return result
    }

    private fun prepareInlineMarkup(buttons: List<InlineButton>): InlineKeyboardMarkup {
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()

        buttons.forEach { button ->
            keyboard.add(mutableListOf(button.getBtn()))
        }

        return InlineKeyboardMarkup(keyboard)
    }

    fun addToFavMenu(upd: Update): AnswerInlineQuery {
        val userId = getUser(upd).id
        val dictionaries = dictionaryRepository.findNotFavoritesForUser(userId)
        return prepareAnswerQuery(dictionaries, upd.inlineQuery.id)
    }

    fun deleteFromFavMenu(upd: Update): AnswerInlineQuery {
        val userId = getUser(upd).id
        val favorites = userRepository.findById(userId).get().favorites
        return prepareAnswerQuery(favorites, upd.inlineQuery.id)
    }

    private fun prepareAnswerQuery(
        dictionaries: Collection<Dictionary>,
        queryId: String
    ): AnswerInlineQuery {
        val results = dictionaries
            .map { prepareDictArticle(it) }

        return AnswerInlineQuery.builder()
            .inlineQueryId(queryId)
            .cacheTime(0)
            .results(results.take(50))
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
}
