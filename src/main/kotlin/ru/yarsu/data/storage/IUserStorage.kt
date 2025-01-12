package ru.yarsu.data.storage

import ru.yarsu.data.model.task.User
import java.util.UUID

interface IUserStorage {
    fun getById(id: UUID): User?
}
