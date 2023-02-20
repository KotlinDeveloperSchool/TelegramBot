package ru.sber.kotlin.school.telegram.bot.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.toggle.BareboneToggle
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.model.User
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.service.MenuService
import ru.sber.kotlin.school.telegram.bot.service.TrainingService
import ru.sber.kotlin.school.telegram.bot.util.Button
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State
import java.util.Collections

@Component
class TrainingBot(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    private val trainingService: TrainingService,
    private val menuService: MenuService,
    private val predicates: Predicates,
    private val botRedisRepository: BotRedisRepository
) : AbilityBot(token, name, BareboneToggle()) {

    private val customSender: CustomSender = CustomSender(
        this.sender,
        this.silent,
        botRedisRepository
    )

    override fun creatorId(): Long = 399762912


    fun menuExtension(): AbilityExtension {
        return MenuExtension(customSender, menuService, predicates)
    }

    fun trainingExtension(): AbilityExtension {
        return TrainingExtension(customSender, trainingService, predicates)
    }



}
