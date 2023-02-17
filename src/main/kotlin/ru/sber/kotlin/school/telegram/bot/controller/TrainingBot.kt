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
import ru.sber.kotlin.school.telegram.bot.game.GameStyle
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.service.TrainingService
import ru.sber.kotlin.school.telegram.bot.util.Button
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import ru.sber.kotlin.school.telegram.bot.util.State

@Component
class TrainingBot(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    @Autowired
    private val trainingService: TrainingService,
    @Autowired
    private val predicates: Predicates,
    private val botRedisRepository: BotRedisRepository
) : AbilityBot(token, name, BareboneToggle()) {

    private val customSender: CustomSender = CustomSender(
        this.sender,
        this.silent,
        botRedisRepository
    )

    override fun creatorId(): Long = 399762912

    fun showAll(): Reply = Reply.of(
        customSender.Router(trainingService::getAllFromRedis)
            .send(),
        predicates.isCommand("all")
    )

    fun drawTrainingButton(): Ability = Ability.builder()
        .name("menu")
        .info("Get main menu")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action(customSender.Router(trainingService::mainMenu)
            .toState(State.Main)
            .botMsg()
            .sendCtx())
        .build()

    fun showFavoriteDicts(): Reply = Reply.of(
        customSender.Router(trainingService::getFavorites)
            .toState(State.Preparation)
            .clearMarkup("Выберите словарь для тренировки")
            .send(),
        predicates.checkState(State.Main)
            .and(predicates.isExactInlineQuery("dicts"))
    )

    fun showGameStyles(): Reply = Reply.of(
        customSender.Router(trainingService::getGameStyles)
            .toState(State.GameStyle)
            .send(),
        predicates.checkState(State.Preparation)
            .and(predicates.startsWithText("Вы выбрали словарь:"))
    )

    fun prepareForGame(): Reply = Reply.of(
        customSender.Router(trainingService::prepareGame)
            .toState(State.BeforeGame)
            .send(),
        predicates.checkState(State.GameStyle)
            .and(predicates.oneOfTitles(GameStyle.titlesSet()))
    )

    fun startGame(): Reply = Reply.of(
        customSender.Router(trainingService::gameRound)
            .toState(State.Game)
            .deleteBotMsg()
            .send(),
        predicates.checkState(State.BeforeGame)
            .and(predicates.hasExactText("Ясно, приступим!"))
    )

    fun processAnswer(): Reply = Reply.of(
        customSender.Router(trainingService::gameAnswer)
            .toState(State.Answer)
            .userMsg()
            .botMsg()
            .send(),
        predicates.checkState(State.Game)
            .and(predicates.isTextAnswer(Button.textsSet()))
    )

    fun resumeGame(): Reply = Reply.of(
        customSender.Router(trainingService::gameRound)
            .toState(State.Game)
            .deleteBotMsg()
            .deleteUserMsg()
            .send(),
        predicates.checkState(State.Answer)
            .and(predicates.hasExactText("Ок, давай дальше!"))
    )

}
