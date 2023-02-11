package ru.sber.developers.bot

import lombok.NoArgsConstructor
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.exceptions.TelegramApiRequestException


@Service
@NoArgsConstructor
class WordsByHeartBot(
    private val userName: String,
    private val token: String
) : TelegramLongPollingBot() {
    private val log: Logger = Logger.getLogger(WordsByHeartBot::class.java)

    val RECONNECT_PAUSE = 10000

    override fun getBotUsername(): String = userName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update) {
        log.debug("Receive new Update. updateID: " + update.updateId)
        val chatId = update.message.chatId
        val inputText = update.message.text
        if (inputText.startsWith("/start")) {
            val message = SendMessage()
            message.setChatId(chatId)
            message.text = "Hello. This is start message"
            try {
                execute(message)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

    fun botConnect() {
        val telegramBotsApi = TelegramBotsApi()
        try {
            telegramBotsApi.registerBot(this)
            log.info("TelegramAPI started. Look for messages")
        } catch (e: TelegramApiRequestException) {
            log.error("Cant Connect. Pause " + RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.message)
            try {
                Thread.sleep(RECONNECT_PAUSE.toLong())
            } catch (e1: InterruptedException) {
                e1.printStackTrace()
                return
            }
            botConnect()
        }
    }
}