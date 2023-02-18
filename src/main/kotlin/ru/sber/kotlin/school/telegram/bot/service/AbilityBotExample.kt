package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository
import ru.sber.kotlin.school.telegram.bot.service.menu.MyDictMenuService
import java.util.*
import kotlin.collections.ArrayList

@Service
class AbilityBotExample(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    val myDictMenuService: MyDictMenuService
) : AbilityBot(token, name) {
    override fun creatorId(): Long = 1234

    /**
    Команда /edit_menu Редактирование словаря, выводит список имеющихся словарей,
    которые были созданы пользователем
     **/

    fun editButtons(): Ability {
        return Ability.builder()
            .name("edit_menu")
            .info("Выберите словарь на редактирование")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val msg = myDictMenuService.getInfoFromAllMyDict(ctx)
                sender.execute(msg)
            }
            .build()
    }
    /**
     * Открываем выпадающий список с нашими словарями
     */
    fun inlineReplyListMyDicts(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val answer = myDictMenuService.getMyDictInfo(upd)
            sender.execute(answer)
        }
        return Reply.of(action, isInlineQueryAllMyDicts())
    }
    private fun isInlineQueryAllMyDicts(): (Update) -> Boolean = { upd: Update ->
        upd.hasInlineQuery() && upd.inlineQuery.query == "allMyDicts"
    }


    /**
    * Ловим выбранный словарь и отправляем в персональное меню словаря
    */
    fun replMessageWithChoseOneMyDict():Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val msg = myDictMenuService.getMyDictMenuAndInfoWord(upd)
            sender.execute(msg)
        }
        return Reply.of(action, isMyDictChose())
    }
    private fun isMyDictChose(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text.startsWith("Меню редактирования словаря:")
    }

    /**
     * Ловим запрос на добавление слова
     */
    fun replMessageWithAddWord():Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val msg = myDictMenuService.isMyWordButton(upd)
            sender.execute(msg)
        }
        return Reply.of(action, isAddWordChose())
    }
    private fun isAddWordChose(): (Update) -> Boolean = { upd: Update ->
        upd.callbackQuery.data.startsWith("add new word to")
    }

    /**
     * Ловим правильный формат ввода нового слова
     */
    fun replGoodAddWord():Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val msg = myDictMenuService.addNewMyWordButton(upd)
            sender.execute(msg)
        }
        return Reply.of(action, isGoodFormatWordForAdd())
    }

    private fun isGoodFormatWordForAdd(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && upd.message.text == "hello - привет"
    }

}