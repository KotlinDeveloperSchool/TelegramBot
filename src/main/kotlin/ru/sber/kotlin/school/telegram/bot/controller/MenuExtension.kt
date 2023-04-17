package ru.sber.kotlin.school.telegram.bot.controller

import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.service.MainMenuService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

class MenuExtension(
    private val customSender: CustomSender,
    private val mainMenuService: MainMenuService,
    private val predicates: Predicates
) : AbilityExtension {

    fun startBot(): Reply = Reply.of(
        customSender.action(mainMenuService::startByUser)
            .send(),
        predicates.isCommand("start")
    )

    fun showAll(): Reply = Reply.of(
        customSender.action(mainMenuService::getAllFromRedis)
            .send(),
        predicates.isCommand("all")
    )

    fun newMainMenu(): Reply = Reply.of(
        customSender.action(mainMenuService::createMainMenu)
            .toState(State.MainMenu)
            .deleteKeyboard("Бот по изучению Английского языка")
            .botMsg()
            .menuMsg()
            .send(),
        predicates.isCommand("menu")
    )

    fun updMainMenu(): Reply = Reply.of(
        customSender.action(mainMenuService::updateMainMenu)
            .toState(State.MainMenu)
            .send(),
        predicates.isCallbackQueryWithData(State.MainMenu.toString())
    )

}
