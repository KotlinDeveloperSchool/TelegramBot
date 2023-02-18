package ru.sber.kotlin.school.telegram.bot.model

import java.util.Collections
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "dictionary")
class Dictionary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val name: String,

    @OneToMany(mappedBy = "dict", fetch = FetchType.LAZY)
    val words: List<Word> = Collections.emptyList(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    val owner: User
)
