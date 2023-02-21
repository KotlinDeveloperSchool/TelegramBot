package ru.sber.kotlin.school.telegram.bot.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.toggle.BareboneToggle
import org.telegram.abilitybots.api.util.AbilityExtension
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.service.CreationService
import ru.sber.kotlin.school.telegram.bot.service.MainMenuService
import ru.sber.kotlin.school.telegram.bot.service.TrainingService
import ru.sber.kotlin.school.telegram.bot.service.DictionaryService
import ru.sber.kotlin.school.telegram.bot.service.DictionaryMenuService
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates

@Component
class TrainingBot(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    private val trainingService: TrainingService,
    private val mainMenuService: MainMenuService,
    private val predicates: Predicates,
    private val botRedisRepository: BotRedisRepository,
    private val dictionaryMenuService: DictionaryMenuService,
    private val dictionaryService: DictionaryService,
    private val creationService: CreationService
) : AbilityBot(token, name, BareboneToggle()) {

    private val customSender: CustomSender = CustomSender(
        this.sender,
        this.silent,
        botRedisRepository
    )

    override fun creatorId(): Long = 399762912


    fun menuExtension(): AbilityExtension {
        return MenuExtension(customSender, mainMenuService, predicates)
    }

    fun trainingExtension(): AbilityExtension {
        return TrainingExtension(customSender, trainingService, predicates)
    }

    fun dictExtension(): AbilityExtension {
        return DictExtension(customSender, predicates, dictionaryMenuService, dictionaryService, sender)
    }

    fun creationExtension(): AbilityExtension {
        return CreationExtension(customSender, creationService, predicates)
    }

}
