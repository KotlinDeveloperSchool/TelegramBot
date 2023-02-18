package ru.sber.kotlin.school.telegram.bot.service.dictionary

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository

@Service
class DictionaryService(
    val dictionaryRep: DictionaryRepository,
    val userRep: UserRepository,
) {
    fun sendCreateDictionaryMsg(chatId: String): SendMessage {
        val message = SendMessage(chatId, "Меню словарей")

        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        row.add(DictionaryMessage.CREATE.message);
        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.isPersistent = true
        message.replyMarkup = keyboardMarkup

        return message
    }

    fun createNewDictionary(upd: Update, dictionaryName: String, userId: Long) {
        var user = userRep.findById(userId).orElse(
            User(
                upd.message.replyToMessage.from.userName,
                upd.message.replyToMessage.from.firstName,
                upd.message.replyToMessage.from.lastName,
                mutableListOf())
        )
        user = userRep.saveAndFlush(user)
        val newDictionary = Dictionary(dictionaryName, mutableListOf(), user)
        dictionaryRep.saveAndFlush(newDictionary)
    }
}