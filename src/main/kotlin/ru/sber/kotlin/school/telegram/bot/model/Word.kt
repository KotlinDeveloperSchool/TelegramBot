package ru.sber.kotlin.school.telegram.bot.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "word")
class Word(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val rus: String,

    val eng: String,

    @ManyToOne
    @JoinColumn(name = "dict_id")
    val dict: Dictionary
)
