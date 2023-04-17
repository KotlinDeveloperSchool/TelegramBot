package ru.sber.kotlin.school.telegram.bot.model.translator

import com.fasterxml.jackson.annotation.JsonProperty

data class Translation(
    @JsonProperty("text")
    val text: String
)