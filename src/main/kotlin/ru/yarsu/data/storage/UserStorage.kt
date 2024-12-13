package ru.yarsu.data.storage

import ru.yarsu.data.model.task.User
import java.util.UUID

class UserStorage(
    private val users: List<User>,
) : IUserStorage {
    override fun getById(id: UUID): User? = users.find { it.id == id }

    override fun sortedWith(comparator: Comparator<in User>): List<User> = users.sortedWith(comparator)

    override fun filter(func: (User) -> Boolean): List<User> = users.filter(func)
}
