package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.service.CreationService
import ru.sber.kotlin.school.telegram.bot.service.TrainingService
import ru.sber.kotlin.school.telegram.bot.util.Button
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.InlQuery
import ru.sber.kotlin.school.telegram.bot.util.InlineButton
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

//    fun showGameStyles(): Reply = Reply.of(
//        customSender.action(trainingService::getGameStyles)
//            .toState(State.GameStyle)
//            .send(),
//        predicates.checkState(State.Preparation)
//            .and(predicates.startsWithText("Вы выбрали словарь:"))
//    )
//
//    fun prepareForGame(): Reply = Reply.of(
//        customSender.action(trainingService::prepareGame)
//            .toState(State.BeforeGame)
//            .send(),
//        predicates.checkState(State.GameStyle)
//            .and(predicates.oneOfTitles(GameStyle.titlesSet()))
//    )
//
//    fun showWordsForLearning(): Reply = Reply.of(
//        customSender.action(trainingService::showWords)
//            .toState(State.WordReminder)
//            .botMsg()
//            .send(),
//        predicates.checkState(State.BeforeGame)
//            .and(predicates.hasExactText(Button.ShowWordsFromDic.text))
//    )
//
//    fun startGame(): Reply = Reply.of(
//        customSender.action(trainingService::gameRound)
//            .toState(State.Game)
//            .deleteBotMsg()
//            .send(),
//        (predicates.checkState(State.BeforeGame).or(predicates.checkState(State.WordReminder)))
//            .and(predicates.hasExactText(Button.GotItLetsGo.text))
//    )
//

//
//    fun resumeGame(): Reply = Reply.of(
//        customSender.action(trainingService::gameRound)
//            .toState(State.Game)
//            .deleteBotMsg()
//            .deleteUserMsg()
//            .send(),
//        predicates.checkState(State.Answer)
//            .and(predicates.hasExactText(Button.OkNext.text))
//    )
}
