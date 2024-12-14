package ru.yarsu.data.storage

import ru.yarsu.data.model.task.User
import java.util.UUID

interface IUserStorage {
    fun getById(id: UUID): User?

    fun sortedWith(comparator: Comparator<in User>): List<User>

    fun filter(func: (User) -> Boolean): List<User>

    fun deleteUser(id: UUID): Boolean
}
