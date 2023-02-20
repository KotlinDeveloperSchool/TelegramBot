package ru.sber.kotlin.school.telegram.bot.model

import java.util.Collections
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "account")
class User(
    @Id
    val id: Long,

    val username: String,
    val firstname: String,
    val lastname: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "dict_id")]
    )
    val favorites: MutableList<Dictionary> = Collections.emptyList()
)
