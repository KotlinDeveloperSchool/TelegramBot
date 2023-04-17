package ru.sber.kotlin.school.telegram.bot.model.translator

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sber.kotlin.school.telegram.bot.model.translator.Translation

data class TranslatorResponse(
    @JsonProperty("translations")
    val translations: List<Translation>
)