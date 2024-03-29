package ru.sber.kotlin.school.telegram.bot.util

import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.abilitybots.api.util.AbilityUtils.getChatId
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import java.io.Serializable

class CustomSender(
    private val sender: MessageSender,
    private val silent: SilentSender,
    private val botRedisRepository: BotRedisRepository
) {

    fun sendText(msg: String): (BaseAbilityBot, Update) -> Unit = { _, upd ->
        silent.send(msg, getChatId(upd))
    }

    fun <T : Serializable, V> sendAnswer(value: V, act: (V) -> BotApiMethod<T>) {
        val answer = act.invoke(value)
        sender.execute(answer)
    }

    fun <T : Serializable> sendAnswerByUpdate(act: (Update) -> BotApiMethod<T>): (BaseAbilityBot, Update) -> Unit =
        { _, upd ->
            val answer = act.invoke(upd)
            sender.execute(answer)
        }

    fun <T : Serializable> sendAnswerByContext(act: (Update) -> BotApiMethod<T>): (MessageContext) -> Unit =
        { ctx ->
            val answer = act.invoke(ctx.update())
            sender.execute(answer)
        }

    fun <T : Serializable, V> sendWithValueByUpdate(value: V, act: (V, Update) -> BotApiMethod<T>):
                (BaseAbilityBot, Update) -> Unit = { _, upd ->
        val answer = act.invoke(value, upd)
        sender.execute(answer)
    }

    fun <T : Serializable> action(action: (Update) -> BotApiMethod<T>) =
        Router(action)

    inner class Router<T : Serializable>(
        private val action: (Update) -> BotApiMethod<T>
    ) {
        private var state: State? = null
        private var clearMarkup: String? = null
        private var deleteKeyboard: String? = null
        private var saveBotMsg = false
        private var saveUserMsg = false
        private var saveMenuMsg = false
        private var deleteBotMsg = false
        private var deleteUserMsg = false
        private var deleteMenuMsg = false
        private var msgText: String? = null
        private var buttons: Array<out InlineButton>? = null

        fun toState(state: State) = apply { this.state = state }

        fun updateMenuMarkup(newText: String, vararg buttons: InlineButton) = apply {
            this.clearMarkup = newText
            this.buttons = buttons
        }

        fun deleteKeyboard(newText: String) = apply { this.deleteKeyboard = newText }

        fun botMsg() = apply { this.saveBotMsg = true }

        fun userMsg() = apply { this.saveUserMsg = true }
        fun menuMsg() = apply { this.saveMenuMsg = true }

        fun deleteBotMsg() = apply { this.deleteBotMsg = true }

        fun deleteUserMsg() = apply { this.deleteUserMsg = true }
        fun deleteMenuMsg() = apply { this.deleteMenuMsg = true }

        fun updateBotMsg(newText: String) = apply { this.msgText = newText }

        fun send(): (BaseAbilityBot, Update) -> Unit =
            { _, upd ->
                doSend(upd)
            }

        fun sendCtx(): (MessageContext) -> Unit =
            { ctx ->
                doSend(ctx.update())
            }

        private fun doSend(upd: Update) {
            try {
                val userId = getUser(upd).id
                val chatId = getChatId(upd).toString()

                deleteKeyboard?.let { deleteKeyboard(chatId, it) }

                if (saveUserMsg && upd.hasMessage())
                    botRedisRepository.putUserMsg(userId, upd.message.messageId)

                state?.let { botRedisRepository.putState(userId, state as State) }

                if (deleteBotMsg) deleteBotMsg(userId, chatId)
                if (deleteUserMsg) deleteUserMsg(userId, chatId)
                if (deleteMenuMsg) deleteMenuMsg(userId, chatId)
                clearMarkup?.let { updateMenuMarkup(userId, chatId) }
                msgText?.let { updateBotMsg(userId, chatId) }

                val answer = action.invoke(upd)

                val msg = sender.execute(answer)
                if (saveBotMsg && msg is Message) botRedisRepository.putBotMsg(userId, msg.messageId)
                if (saveMenuMsg && msg is Message) botRedisRepository.putMenuMsg(userId, msg.messageId)
            } catch (e: Exception) {
                println(e.message)
            }
        }

        private fun updateMenuMarkup(userId: Long, chatId: String) {
            val prevMsg = botRedisRepository.getBotMsg(userId)
            if (prevMsg != null) {
                val editMsg = EditMessageText.builder()
                    .chatId(chatId)
                    .text(clearMarkup!!)
                    .messageId(prevMsg.toInt())
                    .replyMarkup(buttons.toInline())
                    .build()
                sender.execute(editMsg)
                botRedisRepository.deleteBotMsg(userId)
            }
        }

        private fun Array<out InlineButton>?.toInline(): InlineKeyboardMarkup? {
            if (this == null) return null

            val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
            this.forEach { button ->
                keyboard.add(mutableListOf(button.getBtn()))
            }

            return InlineKeyboardMarkup(keyboard)
        }

        private fun deleteKeyboard(chatId: String, text: String) {
            val removeKeyboard = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(ReplyKeyboardRemove(true))
                .build()
            sender.execute(removeKeyboard)
        }

        private fun updateBotMsg(userId: Long, chatId: String) {
            val msgId = botRedisRepository.getBotMsg(userId)
            if (msgId != null) {
                val newMsg = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(msgId.toInt())
                    .text(msgText!!)
                    .build()
                sender.execute(newMsg)
            }
            botRedisRepository.deleteBotMsg(userId)
        }

        private fun deleteBotMsg(userId: Long, chatId: String) {
            val msgId = botRedisRepository.getBotMsg(userId)
            deleteMsg(chatId, msgId)
            botRedisRepository.deleteBotMsg(userId)
        }

        private fun deleteUserMsg(userId: Long, chatId: String) {
            val msgId = botRedisRepository.getUserMsg(userId)
            deleteMsg(chatId, msgId)
            botRedisRepository.deleteUserMsg(userId)
        }

        private fun deleteMenuMsg(userId: Long, chatId: String) {
            val msgId = botRedisRepository.getMenuMsg(userId)
            deleteMsg(chatId, msgId)
            botRedisRepository.deleteUserMsg(userId)
        }

        private fun deleteMsg(chatId: String, msgId: String?) {
            if (msgId != null) {
                val delMsg = DeleteMessage(chatId, msgId.toInt())
                sender.execute(delMsg)
            }
        }
    }
}
