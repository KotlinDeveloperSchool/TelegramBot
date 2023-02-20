package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.service.DictionaryService
import ru.sber.kotlin.school.telegram.bot.service.DictionaryMenuService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.InlQuery
import ru.sber.kotlin.school.telegram.bot.util.State

class DictExtension(
    private val customSender: CustomSender,
    private val predicates: Predicates,
    private val dictionaryMenuService: DictionaryMenuService,
    private val dictionaryService:
    DictionaryService,
    private val sender: MessageSender
)  : AbilityExtension {

    fun dictMenu(): Reply = Reply.of(
        customSender.action(dictionaryMenuService::getDictMenu)
            .toState(State.DictMenu)
            .botMsg()
            .send(),
        predicates.checkState(State.MainMenu)
            .and(predicates.isCallbackQueryWithData("dictMenu"))
    )

    fun addDictList(): Reply = Reply.of(
        customSender.action(dictionaryMenuService::addToFavMenu)
            .toState(State.Adding)
            .send(),
        predicates.checkState(State.DictMenu)
            .and(predicates.isExactInlineQuery(InlQuery.AllDictionaries.text))
    )

    fun deleteDictList(): Reply = Reply.of(
        customSender.action(dictionaryMenuService::deleteFromFavMenu)
            .toState(State.Deleting)
            .send(),
        predicates.checkState(State.DictMenu)
            .and(predicates.isExactInlineQuery(InlQuery.AllFavorites.text))
    )

    fun addedDictToFav(): Reply = Reply.of(
        customSender.action(dictionaryService::addDictionaryToFavorites)
            .toState(State.DictMenu)
            .userMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkState(State.Adding)
            .and(predicates.startsWithText("Вы выбрали словарь:"))
    )

    fun delDictFromFav(): Reply = Reply.of(
        customSender.action(dictionaryService::deleteDictionaryFromFavorites)
            .toState(State.DictMenu)
            .userMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkState(State.Deleting)
            .and(predicates.startsWithText("Вы выбрали словарь:"))
    )

//    fun toMainMenu(): Reply = Reply.of(
//
//    )


    /**
     * В дальнейшем для перехода из меню по прочим кнопкам
     */

    fun inlineReplyTemporary(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            //временная реализация
            sender.execute(
                SendMessage(
                    AbilityUtils.getChatId(upd).toString(),
                    "Временно, далее здесь методы перехода в меню или указания на создание темы нового словаря " +
                            "(можно ввод пользователем темы сделать через inlinequery как в примере с fails - " +
                            "вываливается fails (переименованный типо после двоеточия введите тему словаря: ) " +
                            "и пользователь вводит тему и и направляет весь этот кусок в чат)")
            )
        }
        return Reply.of(action, isInlineQueryTemporary())
    }

    private fun isInlineQueryTemporary(): (Update) -> Boolean = { upd: Update ->
        upd.hasCallbackQuery() && upd.callbackQuery.data == "temporary"
    }
}
