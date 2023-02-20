package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.service.TrainingService
import ru.sber.kotlin.school.telegram.bot.util.Button
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.InlQuery
import ru.sber.kotlin.school.telegram.bot.util.InlineButton
import ru.sber.kotlin.school.telegram.bot.util.State

class TrainingExtension(
    private val customSender: CustomSender,
    private val trainingService: TrainingService,
    private val predicates: Predicates
) : AbilityExtension {

    fun showFavoriteDicts(): Reply = Reply.of(
        customSender.action(trainingService::getFavorites)
            .toState(State.Preparation)
            .updateMenuMarkup("Выберите словарь для тренировки")
//            .deleteMenuMsg()
            .send(),
        predicates.checkState(State.MainMenu)
            .and(predicates.isExactInlineQuery(InlQuery.AllFavorites.text))
    )

    fun showGameStyles(): Reply = Reply.of(
        customSender.action(trainingService::getGameStyles)
            .toState(State.GameStyle)
            .send(),
        predicates.checkState(State.Preparation)
            .and(predicates.startsWithText("Вы выбрали словарь:"))
    )

    fun prepareForGame(): Reply = Reply.of(
        customSender.action(trainingService::prepareGame)
            .toState(State.BeforeGame)
            .send(),
        predicates.checkState(State.GameStyle)
            .and(predicates.oneOfTitles(GameStyle.titlesSet()))
    )

    fun showWordsForLearning(): Reply = Reply.of(
        customSender.action(trainingService::showWords)
            .toState(State.WordReminder)
            .botMsg()
            .send(),
        predicates.checkState(State.BeforeGame)
            .and(predicates.hasExactText(Button.ShowWordsFromDic.text))
    )

    fun startGame(): Reply = Reply.of(
        customSender.action(trainingService::gameRound)
            .toState(State.Game)
            .deleteBotMsg()
            .send(),
        (predicates.checkState(State.BeforeGame).or(predicates.checkState(State.WordReminder)))
            .and(predicates.hasExactText(Button.GotItLetsGo.text))
    )

    fun processAnswer(): Reply = Reply.of(
        customSender.action(trainingService::gameAnswer)
            .toState(State.Answer)
            .userMsg()
            .botMsg()
            .send(),
        predicates.checkState(State.Game)
            .and(predicates.isTextAnswer(Button.textsSet()))
    )

    fun resumeGame(): Reply = Reply.of(
        customSender.action(trainingService::gameRound)
            .toState(State.Game)
            .deleteBotMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkState(State.Answer)
            .and(predicates.hasExactText(Button.OkNext.text))
    )
}
