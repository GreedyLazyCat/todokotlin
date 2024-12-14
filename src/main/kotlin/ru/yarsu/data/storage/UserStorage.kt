package ru.yarsu.data.storage

import ru.yarsu.data.model.task.User
import java.util.UUID

class UserStorage(
    private val initUsers: List<User>,
) : IUserStorage {
    private val users = initUsers.toMutableList()

    override fun getById(id: UUID): User? = users.find { it.id == id }

    override fun sortedWith(comparator: Comparator<in User>): List<User> = users.sortedWith(comparator)

    override fun filter(func: (User) -> Boolean): List<User> = users.filter(func)

    override fun deleteUser(id: UUID): Boolean {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) {
            return false
        }
        users.removeAt(index)
        return true
    }

    override fun getUsers(): List<User> = users.toList()

    override fun addUser(user: User) {
        users.add(user)
    }
}
