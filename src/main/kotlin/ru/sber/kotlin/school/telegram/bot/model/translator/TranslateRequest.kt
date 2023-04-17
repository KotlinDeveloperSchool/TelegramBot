package ru.sber.kotlin.school.telegram.bot.model.translator

data class TranslateRequest(
    val texts: List<String>,
    val targetLanguageCode: String,
    val sourceLanguageCode: String
)