package ru.sber.kotlin.school.telegram.bot.service

import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.hystrix.HystrixFeign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.model.translator.TranslateRequest
import ru.sber.kotlin.school.telegram.bot.model.translator.TranslationLanguages
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import ru.sber.kotlin.school.telegram.bot.service.api.FallbackApi
import ru.sber.kotlin.school.telegram.bot.service.api.TranslatorAPI
import java.util.concurrent.TimeUnit

@Service
class TranslateService(
    @Value("\${telegram.bot.translator.api_key.yandex}")
    private val API_KEY: String,
    private val botRedisRepository: BotRedisRepository
) {
    fun begunFrase(upd: Update): EditMessageText {
        val userId = AbilityUtils.getUser(upd).id
        val chatId = AbilityUtils.getChatId(upd)
        val menuMsgId = botRedisRepository.getMenuMsg(userId)
        botRedisRepository.putTranslatedLang(userId, upd.callbackQuery.data)

        return EditMessageText.builder()
            .chatId(chatId)
            .messageId(menuMsgId!!.toInt())
            .text("Введите текст для перевода")
            .build()
    }

    fun getTranslate(upd: Update): SendMessage = SendMessage.builder()
            .chatId(AbilityUtils.getChatId(upd).toString())
            .text(translator(upd))
            .build()

    fun translator(upd: Update): String{
        val userId = AbilityUtils.getUser(upd).id
        val lang = TranslationLanguages(botRedisRepository.getTranslatedLang(userId)).get()
        val text = upd.message.text.toString()
        val translatorAPI = HystrixFeign.builder()
            .client(ApacheHttpClient())
            .encoder(JacksonEncoder())
            .decoder(JacksonDecoder())
            .options(Request.Options(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true))
            .target(TranslatorAPI::class.java, "https://translate.api.cloud.yandex.net/translate/v2", FallbackApi())

        return translatorAPI.getTranslate(
            API_KEY, TranslateRequest(listOf(text), lang.first(), lang.last())
        ).translations.first().text
    }
}
