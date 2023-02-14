 package ru.sber.kotlin.school.telegram.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WordsByHeartBotApplication

fun main(args: Array<String>) {
	runApplication<WordsByHeartBotApplication>(*args)
}
