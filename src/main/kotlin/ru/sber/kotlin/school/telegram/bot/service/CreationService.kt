package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
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

    fun creation(upd: Update): EditMessageText =
        buildMessageWithText("Введите название словаря$attention", upd)


    fun createDict(upd: Update): EditMessageText {
        var messageText = saveNewDictionary(getUser(upd).id, upd.message.text)

        return buildMessageWithText(messageText, upd)
    }

    private fun saveNewDictionary(userId: Long, dictName: String): String {
        val user = userRepository.findById(userId).get()
        var dictionary = Dictionary(name = dictName, owner = user)
        dictionary = dictionaryRepository.save(dictionary)
        botRedisRepository.putEditDict(userId, dictionary.id)
        return "Создан словарь '${dictionary.name}'\n Теперь введите слово и его " +
                "перевод, порядок слов неважен$attention"
    }

    fun addWord(upd: Update): EditMessageText {
        val messageText = saveNewWord(getUser(upd).id, upd.message.text)

        return buildMessageWithText(messageText, upd)
    }

    private fun saveNewWord(userId: Long, wordInput: String): String {
        val dictId = botRedisRepository.getEditDict(userId)!!.toLong()
        val dictionary = dictionaryRepository.findById(dictId).get()
        val eng = engPattern.find(wordInput)!!.groupValues[0]
        val rus = rusPattern.find(wordInput)!!.groupValues[0]

        var word = Word(rus = rus, eng = eng, dic = dictionary)
        word = wordRepository.save(word)
        val words = wordRepository.findAllByDictionaryId(dictId)

        return "Редактируемый словарь '${dictionary.name}':${getWordsText(words)}"
    }

    fun wrongWord(upd: Update): EditMessageText =
        buildMessageWithText("Слово '${upd.message.text}' не соответствует правилам ввода. Вот пара примеров:\n1) polar bear " +
                    "белый медведь\n2) белый медведь polar bear", upd)

    private fun buildMessageWithText(msgText: String, upd: Update): EditMessageText {
        val userId = getUser(upd).id
        val chatId = getChatId(upd)
        val menuMsgId = botRedisRepository.getMenuMsg(userId)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(menuMsgId!!.toInt())
            .text(msgText)
            .replyMarkup(prepareInlineMarkup(listOf(InlineButton.DictMenu, InlineButton.MainMenu)))
            .build()
    }

    private fun prepareInlineMarkup(buttons: List<InlineButton>): InlineKeyboardMarkup {
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()

        buttons.forEach { button ->
            keyboard.add(mutableListOf(button.getBtn()))
        }

        return InlineKeyboardMarkup(keyboard)
    }

    private fun getWordsText(words: Collection<Word>): String {
        var result = ""
        words.forEachIndexed { i, word ->
            result += "\n${i + 1}. ${word.eng} - ${word.rus}"
        }

        return result
    }
}
