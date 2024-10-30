package ru.yarsu.data.factory

import ru.yarsu.data.model.task.User
import java.time.LocalDateTime
import java.util.UUID

class UserFactory {
    fun createFromStringList(list: List<String>): User? {
        try {
            return User(
                id = UUID.fromString(list[0]),
                login = list[1],
                registrationDateTime = LocalDateTime.parse(list[2]),
                email = list[3],
            )
        } catch (e: Exception) {
            return null
        }
    }
}
