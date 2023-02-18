package ru.sber.kotlin.school.telegram.bot.service.menu

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.model.Dictionary
import ru.sber.kotlin.school.telegram.bot.service.database.MyDictService

@Service
class MyDictMenuService(val myDictService: MyDictService) {
    fun getInfoFromAllMyDict(ctx: MessageContext): SendMessage {
        val chatId = ctx.chatId().toString()
        var message: SendMessage
        // Create InlineKeyboardMarkup object
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        var buttons: MutableList<InlineKeyboardButton> = ArrayList()
        var button: InlineKeyboardButton

        message = SendMessage(chatId,"Меню словарей")
        button = InlineKeyboardButton("Редактировать словарь")
        button.switchInlineQueryCurrentChat = "allMyDicts"
        buttons.add(button)
        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = inlineKeyboardMarkup
        return message

    }
    fun getMyDictInfo(upd: Update) : AnswerInlineQuery {
        val query = upd.inlineQuery
        val userId = AbilityUtils.getUser(upd).id
        val queryId = query.id
        val results = mutableListOf<InlineQueryResult>()
        var my_dict_in_buttons: List<Dictionary> = listOf()
        when (isMyDictExist(userId)) {
            true -> {my_dict_in_buttons = myDictService.getfindAllMyDictionary(userId)}

            false -> {my_dict_in_buttons = listOf()}
        }
        my_dict_in_buttons.forEach { dict ->
                val queryResult = InlineQueryResultArticle.builder().id(dict.id.toString())
                    .title(dict.name)
                    .inputMessageContent(
                        InputTextMessageContent.builder()
                            .messageText("Меню редактирования словаря: ${dict.name}")
                            .build()
                    )
                    .build()
                results.add(queryResult)
            }
        return AnswerInlineQuery.builder()
            .inlineQueryId(queryId)
            .cacheTime(0)
            .results(results.take(50))
            .build()
    }

    fun getMyDictMenuAndInfoWord(upd: Update) : SendMessage {
        val chatId = AbilityUtils.getChatId(upd).toString()
        val userId = AbilityUtils.getUser(upd).id
        val my_dict_name = upd.message.text.substringAfterLast(":").filterNot { it.isWhitespace() }
        var message : SendMessage
        // Create InlineKeyboardMarkup object
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        // Create the keyboard (list of InlineKeyboardButton list)
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        // Create a list for buttons (the first row)
        var buttons: MutableList<InlineKeyboardButton> = ArrayList()
        var button : InlineKeyboardButton
        // есть или нет у пользователя нет слов словаре
        when(isMyWordExist(userId, my_dict_name)){
            true -> {
                message = SendMessage(chatId,
                    "Список пар слов у Вас в словаре - ${my_dict_name} : ${getListMyWords(userId, my_dict_name)}")
                //create another new row
                //buttons = ArrayList()
                button = InlineKeyboardButton("Добавьте новую пару слов")
                button.callbackData = "add new word to : ${my_dict_name}"
                buttons.add(button)
                //keyboard.add(buttons)
                //create another new row

                //buttons = ArrayList()
                button = InlineKeyboardButton("Удалить пару слов")
                button.callbackData = "temporary"
                buttons.add(button)
                //keyboard.add(buttons)
            }
            false -> {
                message = SendMessage(chatId,
                    "У вас нет слов в словаре, добавте новые")
                buttons = ArrayList()
                button = InlineKeyboardButton("Добавьте новую пару слов")
                button.callbackData = "temporary"
                buttons.add(button)
                //keyboard.add(buttons)
            }

        }
        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        message.replyMarkup = inlineKeyboardMarkup
        return message
    }

    fun isMyWordButton(upd: Update) : SendMessage {
        val chatId = AbilityUtils.getChatId(upd).toString()
        var message : SendMessage
        message = SendMessage(chatId,"Введите новую пару слов в формате <hello - привет>")
        return message
    }

    fun addNewMyWordButton(upd: Update) : SendMessage {
        val chatId = AbilityUtils.getChatId(upd).toString()
        val userId = AbilityUtils.getUser(upd).id
        val my_dict_name = "мойСловарь" // Данный пункт нужно как-то брать из прошлых запросов

        //Сюда добавить ошибки (проверка на наличиеб корректность заполнения и т.д)
        val rus = upd.message.text.substringAfterLast("-").filterNot { it.isWhitespace() }
        val eng = upd.message.text.substringBeforeLast("-").filterNot { it.isWhitespace() }
        myDictService.addNewWordInDict(userId,my_dict_name,eng, rus)
        var message : SendMessage
        message = SendMessage(chatId,"Пара слов ${upd.message.text} добавлена в словарь")
        return message
    }


     private fun isMyDictExist(userId : Long) : Boolean {
        val userDicts = myDictService.getfindAllMyDictionary(userId)
        require(userDicts.isNotEmpty()) { return false }
        return true
    }


    private fun isMyWordExist(userId : Long, theme: String) : Boolean {
        val userWordsInDict = myDictService.getAllWordsInMyDict(userId, theme)
        require(userWordsInDict.isNotEmpty()) { return false }
        return true
    }
    fun getListMyWords(userId : Long, theme: String) : String {
        var result = ""
        myDictService.getAllWordsInMyDict(userId, theme).forEach { my_word ->
            result += "\n--||--\n${my_word.rus} - ${my_word.eng} "
        }
        return result
    }

}