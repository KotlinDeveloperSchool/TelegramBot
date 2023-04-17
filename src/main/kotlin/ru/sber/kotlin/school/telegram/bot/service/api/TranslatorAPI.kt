package ru.sber.kotlin.school.telegram.bot.service.api

import feign.Headers
import feign.Param
import feign.RequestLine
import ru.sber.kotlin.school.telegram.bot.model.translator.TranslateRequest
import ru.sber.kotlin.school.telegram.bot.model.translator.TranslatorResponse

interface TranslatorAPI {
    @RequestLine("POST /translate")
    @Headers("Content-Type: application/json", "Authorization: Api-Key {API_KEY}")
    fun getTranslate(@Param("API_KEY") API_KEY: String, request: TranslateRequest): TranslatorResponse
}