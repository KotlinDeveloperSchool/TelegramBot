package ru.sber.kotlin.school.telegram.bot.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "dictionary")
data class Dictionary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int,

    private val name: String,

    @OneToMany
    @JoinColumn(name = "dict_id")
    private val words: List<Word>,

    @OneToOne
    @JoinColumn(name = "owner_id")
    private val owner: User
)
