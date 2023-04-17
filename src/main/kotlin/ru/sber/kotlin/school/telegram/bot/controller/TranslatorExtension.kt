package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.service.TranslatorMenuService
import ru.sber.kotlin.school.telegram.bot.service.TranslateService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

class TranslatorExtension(
    private val customSender: CustomSender,
    private val predicates: Predicates,
    private val translatorMenuService: TranslatorMenuService,
    private val translateService: TranslateService
) : AbilityExtension {

    fun translateMenu(): Reply = Reply.of(
        customSender.action(translatorMenuService::getTranslateMenu)
            .toState(State.TranslateMenu)
            .botMsg()
            .send(),
        predicates.checkState(State.MainMenu)
            .and(predicates.isCallbackQueryWithData("translateMenu"))
    )

    fun startTranslate(): Reply = Reply.of(
        customSender.action(translateService::begunFrase)
            .toState(State.PrepareTranslate)
            .botMsg()
            .send(),
        predicates.checkState(State.TranslateMenu)
            .and(predicates.isCallbackQueryWithData("translateToEng")
                .or(predicates.isCallbackQueryWithData("translateToRus")))
    )

    fun translator(): Reply = Reply.of(
        customSender.action(translateService::getTranslate)
            .toState(State.Translator)
            .botMsg()
            .send(),
        predicates.checkState(State.PrepareTranslate)
            .or(predicates.checkState(State.Translator))
    )

}