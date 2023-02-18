package ru.sber.kotlin.school.telegram.bot.game

import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.exception.ActionException

enum class GameStyle(
    private val title: String,
    private val description: String
    ) {
    OneOfFour(
        "На выбор из четырёх", "На экране будет представлено слово и 4 варианта " +
                "перевода, Вам необходимо выбрать правильный"
    ),
    FreeType(
        "На свободный ввод", "На экране будет представлено слово, Вам необходимо " +
                "ввести и отправить его перевод"
    );

    fun title() = title
    fun desc() = description

    companion object {
        private val map = GameStyle.values().associateBy { it.title }
        private fun get(title: String): GameStyle = map[title]
            ?: throw ActionException("Not found game style '$title'")

        fun getStyle(upd: Update): GameStyle = get(upd.message.text)

        fun getDescription(upd: Update): String = getStyle(upd).description

        fun getName(upd: Update): String = getStyle(upd).toString()

        fun titlesSet(): Set<String> = map.keys
    }
}
