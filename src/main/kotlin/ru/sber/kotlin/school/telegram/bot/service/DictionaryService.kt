package ru.sber.kotlin.school.telegram.bot.service

import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.util.AbilityUtils.getUser
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository

@Service
class DictionaryService(
    private val dictionaryRepository: DictionaryRepository,
    private val userRepository: UserRepository,
    private val dictionaryMenuService: DictionaryMenuService
) {

    fun addDictionaryToFavorites(upd: Update): EditMessageText =
        processFavorites(upd, true)


    fun deleteDictionaryFromFavorites(upd: Update): EditMessageText =
        processFavorites(upd, false)


    private fun processFavorites(upd: Update, isAdding: Boolean): EditMessageText {
        val userId = getUser(upd).id
        val theme = upd.message.text.substringAfter('\n')
        val user = userRepository.findById(userId).get()
        val dictionary = dictionaryRepository.findByNameForUser(theme, userId).get()

        if (isAdding)
            user.favorites.add(dictionary)
        else
            user.favorites.remove(dictionary)

        userRepository.save(user)

        return dictionaryMenuService.getDictMenu(upd)
    }
}
