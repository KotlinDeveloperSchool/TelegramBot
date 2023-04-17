package ru.sber.kotlin.school.telegram.bot.model.translator

class TranslationLanguages(
    var lang: String?
){
    fun get(): List<String> {
        return when(lang){
            "TranslateToEng" -> listOf("en","ru")
            "TranslateToRus" -> listOf("ru","en")
            else -> {throw Exception("Не подобраны языки перевода")}
        }
    }
}