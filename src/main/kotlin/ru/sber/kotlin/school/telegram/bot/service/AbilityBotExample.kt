package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository

@Component
class AbilityBotExample(
    @Value("\${telegram.bot.token}")
    private val token: String,
    @Value("\${telegram.bot.name}")
    private val name: String,
    private var userRepository: UserRepository
) : AbilityBot(token, name) {
    override fun creatorId(): Long = 1234

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

    fun start(): Ability {
        return Ability.builder()
            .name("start")
            .info("starts bot")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                if (!userRepository.findById(ctx.chatId().toInt()).isPresent) {
                    val chatId = ctx.chatId()
                    val user = ru.sber.kotlin.school.telegram.bot.model.User()
                    user.id = chatId
                    user.userName = ctx.user().userName
                    user.firstName = ctx.user().firstName
                    user.lastName = ctx.user().lastName
                    userRepository.save(user)
                }

//                val data = prepareUserData(user)
//                silent.send(data, ctx.chatId())
            }
            .post { ctx -> silent.send("Bye world!", ctx.chatId()) }
            .build()
    }


    /**
     * Обработка текстовых сообщений от пользователя,
     * предикат isNotCommandUpdate() проверяет, что текст не является командой
     */
    fun echoReplyMessage(): Reply {
        val action: (BaseAbilityBot, Update) -> Unit = { _, upd ->
            silent.send(
                "${upd.message.from.firstName} said ${upd.message.text.uppercase()}",
                upd.message.chatId
            )
        }
        return Reply.of(action, isNotCommandUpdate())
    }

    private fun isNotCommandUpdate(): (Update) -> Boolean = { upd: Update ->
        upd.hasMessage() && upd.message.hasText() && !upd.message.text.startsWith("/")
    }

    /**
     * Команда /keyBtn отправляет пользователю кнопки в районе клавиатуры
     */
    fun drawKeyboardButtons(): Ability = Ability.builder()
        .name("keyBtn")
        .info("Get keyboardButtons")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            sendCustomKeyboard(ctx.chatId().toString())
        }
        .build()

    fun sendCustomKeyboard(chatId: String) {
        val message = SendMessage(chatId, "Message with keyboard buttons")

        // Create ReplyKeyboardMarkup object
        val keyboardMarkup = ReplyKeyboardMarkup()
        // Create the keyboard (list of keyboard rows)
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        // Create a keyboard row
        var row = KeyboardRow()
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Row 1 Button 1");
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
        // Add it to the message
        message.replyMarkup = keyboardMarkup

        // Send the message
        execute(message);
    }

    /**
     * Команда /inlBtn отправляет пользователю кнопки под сообщением
     */
    fun drawInlineButtons(): Ability = Ability.builder()
        .name("inlBtn")
        .info("Get keyboardButtons")
        .locality(Locality.USER)
        .privacy(Privacy.PUBLIC)
        .action { ctx ->
            sendInlineKeyboard(ctx.chatId().toString())
        }
        .build()

    fun sendInlineKeyboard(chatId: String) {
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
        // Initialize each button, the text must be written
        val github: InlineKeyboardButton = InlineKeyboardButton("github")
        // Also must use exactly one of the optional fields,it can edit  by set method
        github.url = "https://github.com"
        // Add button to the list
        buttons.add(github)
        keyboard.add(buttons)
        inlineKeyboardMarkup.keyboard = keyboard
        // Add it to the message
        message.replyMarkup = inlineKeyboardMarkup

        execute(message);
    }

    /**
     * Пока НЕ получилось реализовать, должен возвращаться список с прокруткой
     */
    private fun doInlineQuery() {
        val q1 = InlineQueryResultArticle.builder().id("1")
            .title("Query1")
            .inputMessageContent(
                InputTextMessageContent.builder()
                    .messageText("This is query 1")
                    .build()
            )
            .build()
        val q2 = InlineQueryResultArticle.builder().id("2")
            .title("Query2")
            .inputMessageContent(
                InputTextMessageContent.builder()
                    .messageText("This is query 2")
                    .build()
            )
            .build()
        val q3 = InlineQueryResultArticle.builder().id("3")
            .title("Query3")
            .inputMessageContent(
                InputTextMessageContent.builder()
                    .messageText("This is query 3")
                    .build()
            )
            .build()
        val q4 = InlineQueryResultArticle.builder().id("4")
            .title("Query4")
            .inputMessageContent(
                InputTextMessageContent.builder()
                    .messageText("This is query 4")
                    .build()
            )
            .build()
        val q5 = InlineQueryResultArticle.builder().id("5")
            .title("Query5")
            .inputMessageContent(
                InputTextMessageContent.builder()
                    .messageText("This is query 5")
                    .build()
            )
            .build()

        val answer = AnswerInlineQuery.builder()
            .inlineQueryId("")
            .isPersonal(true)
            .results(listOf(q1, q2, q3, q4, q5))
            .build()
        sender.execute(answer)
    }
}
