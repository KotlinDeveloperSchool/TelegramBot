package ru.sber.kotlin.school.telegram.bot.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "account")
 class User(
    @Id
    var id: Long,

    val username: String,
    val firstname: String,
    val lastname: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "dict_id")]
    )
    //требуется изменяемая коллекция для записи
    private var favorites: List<Dictionary> = Collections.emptyList()
)
