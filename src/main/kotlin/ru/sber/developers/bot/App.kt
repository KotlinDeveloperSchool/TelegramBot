package ru.sber.developers.bot

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer

object App {
    private val log: Logger = Logger.getLogger(App::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        ApiContextInitializer.init()
        val wordsByHeartBot = WordsByHeartBot("WordsByHeartBot", "6260464483:AAGpphfGTY_LDG9x84wWm4XnQ-WudDHS2T4")
        wordsByHeartBot.botConnect()
    }
}