package ru.sber.kotlin.school.telegram.bot.service.dictionary

enum class DictionaryMessage(val message: String) {
    CREATE("Создать новый словарь"),
    NEW_DICTIONARY("Введите название словаря")
    ;
}