package ru.sber.kotlin.school.telegram.bot.controller

import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.service.MenuService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

class MenuExtension(
    private val customSender: CustomSender,
    private val menuService: MenuService,
    private val predicates: Predicates
    ) : AbilityExtension {

    fun startBot(): Reply = Reply.of(
        customSender.action(menuService::startByUser)
            .send(),
        predicates.isCommand("start")
    )

    fun showAll(): Reply = Reply.of(
        customSender.action(menuService::getAllFromRedis)
            .send(),
        predicates.isCommand("all")
    )

    fun mainMenu(): Reply = Reply.of(
        customSender.action(menuService::mainMenu)
            .toState(State.Main)
            .deleteKeyboard("Переходим в главное меню!")
            .botMsg()
            .send(),
        predicates.isCommand("menu")
    )

}
