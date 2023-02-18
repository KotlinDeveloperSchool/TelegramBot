package ru.sber.kotlin.school.telegram.bot.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "dictionary")
class Dictionary(
    val name: String,

    @OneToMany(mappedBy = "dic", fetch = FetchType.LAZY)
    val words: List<Word> = Collections.emptyList(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    val owner: User,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
