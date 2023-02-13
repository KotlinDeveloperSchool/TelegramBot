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
 class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,

    @Column(name = "tg_id")
    private var telegramId: Long = 0 ,

    private var username: String = "",
    private var firstname: String = "",
    private var lastname: String = "",

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "dict_id")]
    )
    private var favorites: List<Dictionary> = listOf()
)
