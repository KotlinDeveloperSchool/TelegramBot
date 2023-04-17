package ru.sber.kotlin.school.telegram.bot.util

enum class State {
    MainMenu, Preparation, GameStyle, BeforeGame, Game, WordReminder, Answer, DictMenu,
    TranslateMenu, TranslateToRus, TranslateToEng, PrepareTranslate, Translator,
    Adding, Deleting, PrepareDict, CreateDict, AddWord
}
