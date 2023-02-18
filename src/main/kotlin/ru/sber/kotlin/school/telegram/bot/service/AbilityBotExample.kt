package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.beans.factory.annotation.Value
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.abilitybots.api.objects.ReplyFlow
import org.telegram.abilitybots.api.objects.ReplyFlow.ReplyFlowBuilder
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.util.CustomSender
import ru.sber.kotlin.school.telegram.bot.util.Predicates
import java.util.function.BiConsumer
import java.util.function.Predicate

//@Component
class AbilityBotExample(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    private val predicates: Predicates,
    private val botRedisRepository: BotRedisRepository
) : AbilityBot(token, name) {
    private val customSender = CustomSender(sender, silent, botRedisRepository)
    private val MAIN_MENU = 1
    private val TRAINING_MENU = 2
    private val DICS_MENU = 3
    private val EMPTY_MENU = 4

    /**
     * Реализация "машины состояний" - ограничение действий в зависимости от текущего этапа
     * Порядок вызова: первым - prepareFlow(), последним buildFlow(),
     * nextFlows() необязательный промежуточный
     */
    @Deprecated(message = "Реализовал самостоятельно в CustomSender.Router")
    fun getFlow(): ReplyFlow {
        val end = Reply.of(
            customSender.sendText("Вы дошли до конца"),
            predicates.isCommand("end")
        )
        val trainFlow = prepareFlow(TRAINING_MENU)
            .nextFlows(end)
            .buildFlow(
                customSender.sendText("Вы попали в меню тренировки"),
                predicates.isCommand("train")
            )

        val dicsFlow = prepareFlow(DICS_MENU)
            .nextFlows(end)
            .buildFlow(
                customSender.sendText("Вы попали в меню словарей"),
                predicates.isCommand("dics")
            )

        val emptyFlow = prepareFlow(TRAINING_MENU)
            .buildFlow(
                customSender.sendText("Вы попали в пустое меню"),
                predicates.isCommand("empty")
            )

        return prepareFlow(MAIN_MENU)
            .nextFlows(trainFlow, dicsFlow, emptyFlow)
            .buildFlow(
                customSender.sendText("Вы попали в главное меню"),
                predicates.isCommand("menu")
            )
    }

    /**
     * Создаёт объект FlowBuilder, может иметь идентификатор этапа, в противном
     * случае будет сгенерирован ботом автоматически
     */
    private fun prepareFlow(id: Int? = null): ReplyFlowBuilder =
        if (id == null) ReplyFlow.builder(db)
        else ReplyFlow.builder(db, id)

    /**
     * Добавляет в FlowBuilder следующие доступные этапы
     */
    private fun ReplyFlowBuilder.nextFlows(vararg replies: Reply): ReplyFlowBuilder {
        replies.forEach {
            if (it is ReplyFlow) this.next(it)
            else this.next(it)
        }
        return this
    }

    /**
     * Добавляет во FlowBuilder действие и условие для исполнения, после этого возвращает сам Flow
     */
    private fun ReplyFlowBuilder.buildFlow(
        act: BiConsumer<BaseAbilityBot, Update>,
        pred: Predicate<Update>
    ): ReplyFlow =
        this.action(act)
            .onlyIf(pred)
            .build()

    override fun creatorId(): Long = 399762912

    /**
     * Создание списков в режиме inline, для перехода в режим нужно:
     * 1. Включить опцию в боте через @BotFather
     * 2. Набрать в поисковой строке @BotName
     * 3. С клавиатурных кнопок в режим не перейти, значит нужно через inlineButtons
     * пример в sendInlineKeyboard() кнопка toInline
     */
    fun echoInlineReply(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val answer = SendMessage(getChatId(upd).toString(), upd.callbackQuery.data)
            sender.execute(answer)
        }
        return Reply.of(action, isInlineQuery())
    }

    /**
     * Так можно выполнять фильтрацию по инлайн командам
     */
    private fun isInlineQuery(): (Update) -> Boolean = { upd: Update ->
        upd.hasCallbackQuery()
    }

    fun processInline(upd: Update): AnswerInlineQuery {
        val query = upd.inlineQuery
        val results = mutableListOf<InlineQueryResult>()

        for (i: Int in 0..5) {
            val queryResult = InlineQueryResultArticle.builder().id("$i")
                .title("Query$i")
                .inputMessageContent(
                    InputTextMessageContent.builder()
                        .messageText("This is query $i")
                        .build()
                )
                .build()
            results.add(queryResult)
        }

        return AnswerInlineQuery.builder()
            .inlineQueryId(query.id)
            .cacheTime(0)
            .switchPmText("Go to bot")
            .switchPmParameter("dict")
            .results(results)
            .build()

    }

    /**
     * Создание команды /hello, которая отправляет пользователю текстовое
     * сообщение с данными его Telegram аккаунт
     */
    fun echoMessage(): Ability {
        return Ability.builder()
            .name("hello")
            .info("says hello world!")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val user = ctx.user()
                val data = prepareUserData(user)
                silent.send(data, ctx.chatId())
            }
            .build()
    }

    private fun prepareUserData(user: User): String = """Hi!
                    ${user.userName} - username
                    ${user.id} - id
                    ${user.isBot} - isBot
                    ${user.canJoinGroups} - canJoin
                    ${user.firstName} - firstName
                    ${user.lastName} - lastName
                    ${user.addedToAttachmentMenu} - AttachmentMenu
                    ${user.canReadAllGroupMessages} - canRead
                    ${user.languageCode} - language
                    ${user.supportInlineQueries} - queries
                    ${user.isPremium} - premium
                """.trimMargin()


    /**
     * Обработка текстовых сообщений от пользователя,
     * предикат isNotCommandUpdate() проверяет, что текст не является командой
     */
    fun echoReplyMessage(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            val msg = SendMessage(
                upd.message.chatId.toString(),
                "${upd.message.from.firstName} said ${upd.message.text.uppercase()}"
            )
            //Удалить кастомную клавиатуру
            msg.replyMarkup = ReplyKeyboardRemove(true)

            sender.execute(msg)
        }

        return Reply.of(action, isNotCommandUpdate())
    }

    private fun isNotCommandUpdate(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && !upd.message.text.startsWith("/")
    }

    /**
     * Команда /keyBtn отправляет пользователю кнопки в районе клавиатуры
     */
    fun drawKeyboardButtons(): Ability {
        return Ability.builder()
            .name("keys")
            .info("Get keyboardButtons")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val msg = sendCustomKeyboard(ctx.chatId().toString())
                sender.execute(msg)
            }
            .build()
    }

    fun sendCustomKeyboard(chatId: String): SendMessage {
        val message = SendMessage(chatId, "Message with keyboard buttons")

        // Create ReplyKeyboardMarkup object
        val keyboardMarkup = ReplyKeyboardMarkup()
        // Create the keyboard (list of keyboard rows)
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        // Create a keyboard row
        var row = KeyboardRow()
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Button1");
        row.add("Row 1 Button 2");
        row.add("Row 1 Button 3");
        // Add the first row to the keyboard
        keyboard.add(row)
        // Create another keyboard row
        row = KeyboardRow();
        // Set each button for the second line
        row.add("Row 2 Button 1");
        row.add("Row 2 Button 2");
        row.add("Row 2 Button 3");
        // Add the second row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.isPersistent = true
        // Add it to the message
        message.replyMarkup = keyboardMarkup

        return message
    }

    /**
     * Команда /inlBtn отправляет пользователю кнопки под сообщением
     */
    fun drawInlineButtons(): Ability = Ability.builder()
        .name("inl")
        .info("Get keyboardButtons")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            val msg = sendInlineKeyboard(ctx.chatId().toString())
            sender.execute(msg)
        }
        .build()

    fun sendInlineKeyboard(chatId: String): SendMessage {
        val message = SendMessage(chatId, "Message with inline buttons")

        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        // Create the keyboard (list of InlineKeyboardButton list)
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        // Create a list for buttons
        val buttons: MutableList<InlineKeyboardButton> = ArrayList()
        // Initialize each button, the text must be written
        val youtube: InlineKeyboardButton = InlineKeyboardButton("youtube")
        // Also must use exactly one of the optional fields,it can edit  by set method
        youtube.url = "https://www.youtube.com"

        // Add button to the list
        buttons.add(youtube)

        val toInline = InlineKeyboardButton("toInlineSuc")
        toInline.switchInlineQueryCurrentChat = "toInline"
        buttons.add(toInline)
        //This fails by predicate isInlineQuery()
        val toInlineFail = InlineKeyboardButton("toInlineFail")
        toInlineFail.switchInlineQueryCurrentChat = "toInlineFail"
        buttons.add(toInlineFail)

        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = inlineKeyboardMarkup

        return message
    }

}
