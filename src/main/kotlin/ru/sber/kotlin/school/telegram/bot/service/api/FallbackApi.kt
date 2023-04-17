package ru.sber.kotlin.school.telegram.bot.service.api

import ru.sber.kotlin.school.telegram.bot.model.translator.TranslateRequest
import ru.sber.kotlin.school.telegram.bot.model.translator.Translation
import ru.sber.kotlin.school.telegram.bot.model.translator.TranslatorResponse

class FallbackApi : TranslatorAPI {
    override fun getTranslate(API_KEY: String, body: TranslateRequest) =
        TranslatorResponse(listOf(Translation("Повторите попытку позже")))
}