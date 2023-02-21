package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.service.DictionaryMenuService
import ru.sber.kotlin.school.telegram.bot.service.DictionaryService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.InlQuery
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

class DictExtension(
    private val customSender: CustomSender,
    private val predicates: Predicates,
    private val dictionaryMenuService: DictionaryMenuService,
    private val dictionaryService: DictionaryService
) : AbilityExtension {

    fun dictMenu(): Reply = Reply.of(
        customSender.action(dictionaryMenuService::getDictMenu)
            .toState(State.DictMenu)
            .botMsg()
            .send(),
        predicates.checkStates(State.MainMenu, State.PrepareDict, State.CreateDict, State.AddWord)
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
}
