package ru.sber.kotlin.school.telegram.bot.util

enum class InlQuery(
    val text: String
) {
    AllDictionaries("dicts"),
    AllFavorites("favs")
}
