package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.service.CreationService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

class CreationExtension(
    private val customSender: CustomSender,
    private val creationService: CreationService,
    private val predicates: Predicates
) : AbilityExtension {


    fun dictionaryCreation(): Reply = Reply.of(
        customSender.action(creationService::creation)
            .toState(State.PrepareDict)
            .send(),
        predicates.checkState(State.DictMenu)
            .and(predicates.isCallbackQueryWithData(State.CreateDict.toString()))
    )

    fun newName(): Reply = Reply.of(
        customSender.action(creationService::createDict)
            .toState(State.CreateDict)
            .userMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkState(State.PrepareDict)
            .and(predicates.isMatchPattern(creationService.dictNamePattern))
    )

    fun newWord(): Reply = Reply.of(
        customSender.action(creationService::addWord)
            .toState(State.AddWord)
            .userMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkStates(State.CreateDict, State.AddWord)
            .and(predicates.isMatchPattern(creationService.fullWordPattern))
    )

    fun failWord(): Reply = Reply.of(
        customSender.action(creationService::wrongWord)
            .toState(State.AddWord)
            .userMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkStates(State.CreateDict, State.AddWord)
            .and(predicates.isTextAnswer(setOf(State.DictMenu.toString(), State.MainMenu.toString())))
    )
}
