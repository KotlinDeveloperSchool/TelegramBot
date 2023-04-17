package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.util.InlineButton

@Service
class TranslatorMenuService(
    private val botRedisRepository: BotRedisRepository
) {
    fun getTranslateMenu(upd: Update): EditMessageText {
        val chatId = AbilityUtils.getChatId(upd).toString()
        val userId = AbilityUtils.getUser(upd).id

        val prevMsg = botRedisRepository.getMenuMsg(userId)

        val messageBuilder = EditMessageText.builder()
            .chatId(chatId)
            .messageId(prevMsg!!.toInt())

        val buttons = mutableListOf(
            InlineButton.TranslateToEnglish,
            InlineButton.TranslateToRussia,
            InlineButton.MainMenu
        )

        messageBuilder.text("Выберите вариант переводчика")
        messageBuilder.replyMarkup(prepareInlineMarkup(buttons))

        return messageBuilder.build()
    }

    private fun prepareInlineMarkup(buttons: List<InlineButton>): InlineKeyboardMarkup {
        val keyboard: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()

        buttons.forEach { button ->
            keyboard.add(mutableListOf(button.getBtn()))
        }

        return InlineKeyboardMarkup(keyboard)
    }
}