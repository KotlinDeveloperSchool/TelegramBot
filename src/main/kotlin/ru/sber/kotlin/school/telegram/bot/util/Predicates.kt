package ru.sber.kotlin.school.telegram.bot.util

import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository
import java.util.function.Predicate

@Component
class Predicates(private val botRedisRepository: BotRedisRepository) {

    fun isExactInlineQuery(query: String): Predicate<Update> = Predicate { upd ->
        upd.hasInlineQuery() && upd.inlineQuery.query.equals(query, true)
    }

    fun isCommand(command: String): Predicate<Update> = Predicate { upd ->
        upd.hasMessage() && upd.message.isCommand && upd.message.text.equals("/$command")
    }

    fun hasExactText(text: String): Predicate<Update> = Predicate { upd ->
        isTextMessage(upd) && upd.message.text.equals("$text", true)
    }

    fun startsWithText(text: String): Predicate<Update> = Predicate { upd ->
        isTextMessage(upd) && upd.message.text.startsWith(text, true)
    }

    fun oneOfTitles(titles: Set<String>): Predicate<Update> = Predicate { upd ->
        isTextMessage(upd) && titles.contains(upd.message.text)
    }

    fun isTextAnswer(titles: Set<String>): Predicate<Update> = Predicate { upd ->
        isTextMessage(upd) && !upd.message.isCommand && !titles.contains(upd.message.text)
    }

    private fun isTextMessage(upd: Update): Boolean =
        upd.hasMessage() && upd.message.hasText()

    fun checkState(expected: State): Predicate<Update> = Predicate { upd ->
        val userId = AbilityUtils.getUser(upd).id
        val actual: String? = botRedisRepository.getState(userId)

        expected.toString().equals(actual, true)
    }
}
