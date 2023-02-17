package ru.sber.kotlin.school.telegram.bot.game

import org.telegram.telegrambots.meta.api.objects.Update

interface GameService {
    fun prepare(userId: Long)
    fun startRound(userId: Long): String
    fun checkAnswer(upd: Update): String
}
