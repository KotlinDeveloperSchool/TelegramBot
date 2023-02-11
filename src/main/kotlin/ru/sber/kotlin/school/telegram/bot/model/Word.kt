package ru.sber.kotlin.school.telegram.bot.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "word")
data class Word(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int,

    private val rus: String,

    private val eng: String
)
