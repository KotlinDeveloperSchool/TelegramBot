package ru.sber.kotlin.school.telegram.bot.util

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
}
