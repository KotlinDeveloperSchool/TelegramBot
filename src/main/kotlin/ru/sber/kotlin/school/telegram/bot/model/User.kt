package ru.sber.kotlin.school.telegram.bot.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "account")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column(name = "tg_id")
    private val telegramId: Long,

    private val username: String,
    private val firstname: String,
    private val lastname: String,

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "dict_id")]
    )
    private val favorites: List<Dictionary>
)
