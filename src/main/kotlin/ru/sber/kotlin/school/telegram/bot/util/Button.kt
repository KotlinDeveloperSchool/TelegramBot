package ru.sber.kotlin.school.telegram.bot.util

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton

enum class InlineButton(
    private val buttonName: String,
) {
    Training("Тренировка") {
        override fun getBtn(): InlineKeyboardButton =
            switchToInlineBtn(InlQuery.AllFavorites.text)
    },
    DictMenu("Словари") {
        override fun getBtn(): InlineKeyboardButton =
            callbackBtn(State.DictMenu.toString())
    },
    AddFromReady("Выбрать для изучени") {
        override fun getBtn(): InlineKeyboardButton =
            switchToInlineBtn(InlQuery.AllDictionaries.text)
    },
    CreateNewDictionary("Создать новый словарь") {
        override fun getBtn(): InlineKeyboardButton =
            callbackBtn(State.CreateDict.toString())
    },

    DeleteFavDictionary("Удалить словарь из списка изучаемых") {
        override fun getBtn(): InlineKeyboardButton =
            switchToInlineBtn(InlQuery.AllFavorites.text)
    },

    MainMenu("В главное меню") {
        override fun getBtn(): InlineKeyboardButton =
            callbackBtn(State.MainMenu.toString())
    };

    abstract fun getBtn(): InlineKeyboardButton

    protected fun callbackBtn(data: String): InlineKeyboardButton = getBuilderWithText()
        .callbackData(data)
        .build()

    protected fun switchToInlineBtn(query: String): InlineKeyboardButton = getBuilderWithText()
        .switchInlineQueryCurrentChat(query)
        .build()

    protected fun getBuilderWithText(): InlineKeyboardButton.InlineKeyboardButtonBuilder =
        InlineKeyboardButton.builder()
            .text(this.buttonName)
}

enum class Button(
    val text: String
) {
    GotItLetsGo("Ясно, приступим!"),
    ShowWordsFromDic("Покажи, что в словаре!"),
    OkNext("Ок, давай дальше!");

    companion object {
        fun textsSet(): Set<String> =
            Button.values().map { it.text }.toSet()
    }

    fun getBtn(): KeyboardButton =
        KeyboardButton(this.text)
}
