package ru.sber.kotlin.school.telegram.bot.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "account")
 class User(
    @Id
    var id: Long = 0,

    private var username: String = "",
    private var firstname: String = "",
    private var lastname: String = "",

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "dict_id")]
    )
    private var favorites: List<Dictionary> = Collections.emptyList()
)
