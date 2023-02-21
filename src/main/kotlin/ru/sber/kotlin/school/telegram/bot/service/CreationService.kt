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
import ru.sber.kotlin.school.telegram.bot.model.Word
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.repository.WordRepository
import ru.sber.kotlin.school.telegram.bot.util.InlineButton

@Service
class CreationService(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
    private val botRedisRepository: BotRedisRepository
) {
    private val attention = "\n!!! разрешены латиница, кириллица и символ пробела"
    val dictNamePattern = "^[\\wа-яА-Я]+( ?[\\wа-яА-Я]+)*\$".toRegex()
    private val engPattern = "([a-zA-Z]+( ?[a-zA-Z]+)*)".toRegex()
    private val rusPattern = "([а-яА-Я]+( ?[а-яА-Я]+)*)".toRegex()
    val fullWordPattern = ("^($rusPattern $engPattern)|($engPattern $rusPattern)\$").toRegex()

    fun creation(upd: Update): EditMessageText {
        val userId = getUser(upd).id

        val chatId = getChatId(upd).toString()
        val prevMsg = botRedisRepository.getMenuMsg(userId)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(prevMsg!!.toInt())
            .text("Введите название словаря$attention")
            .replyMarkup(prepareInlineMarkup(listOf(InlineButton.DictMenu, InlineButton.MainMenu)))
            .build()
    }

    fun createDict(upd: Update): EditMessageText {
        val userId = getUser(upd).id

        val user = userRepository.findById(userId).get()
        val dictName = upd.message.text
        var dictionary = Dictionary(name = dictName, owner = user)
        dictionary = dictionaryRepository.save(dictionary)
        botRedisRepository.putEditDict(userId, dictionary.id)

        val chatId = getChatId(upd)
        val menuMsgId = botRedisRepository.getMenuMsg(userId)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(menuMsgId!!.toInt())
            .text("Создан словарь '${dictionary.name}'\n Теперь введите слово и его перевод, порядок слов неважен$attention")
            .replyMarkup(prepareInlineMarkup(listOf(InlineButton.DictMenu, InlineButton.MainMenu)))
            .build()
    }

    fun addWord(upd: Update): EditMessageText {
        val userId = getUser(upd).id

        val dictId = botRedisRepository.getEditDict(userId)!!
        val dictionary = dictionaryRepository.findById(dictId.toLong()).get()
        val words = upd.message.text
        val eng = engPattern.find(words)!!.groupValues[0]
        val rus = rusPattern.find(words)!!.groupValues[0]

        var word = Word(rus = rus, eng = eng, dic = dictionary)
        word = wordRepository.save(word)
        val chatId = getChatId(upd)
        val menuMsgId = botRedisRepository.getMenuMsg(userId)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(menuMsgId!!.toInt())
            .text("В словарь '${dictionary.name}' добавлено слово:\n${word.rus} - ${word.eng}")
            .replyMarkup(prepareInlineMarkup(listOf(InlineButton.DictMenu, InlineButton.MainMenu)))
            .build()
    }

    fun wrongWord(upd: Update): EditMessageText {
        val userId = getUser(upd).id

        val words = upd.message.text

        val chatId = getChatId(upd)
        val menuMsgId = botRedisRepository.getMenuMsg(userId)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(menuMsgId!!.toInt())
            .text("Слово '$words' не соответствует правилам ввода. Вот пара примеров:\n1) polar bear " +
                    "белый медведь\n2) белый медведь polar bear")
            .replyMarkup(prepareInlineMarkup(listOf(InlineButton.DictMenu, InlineButton.MainMenu)))
            .build()
    }
    private fun getFavorites(userId: Long) =
        userRepository.findById(userId).get().favorites

    private fun getFavoriteText(favorites: Collection<Dictionary>): String {
        var result = ""
        favorites.forEachIndexed() { i, favDictionary ->
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
