package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task
import java.util.UUID
import kotlin.Comparator

interface ITaskStorage {
    fun sortedWith(comparator: Comparator<in Task>): List<Task>

    fun getById(id: UUID): Task?

    fun filter(func: (Task) -> Boolean): List<Task>

    fun getTasks(): List<Task>

    fun addTask(task: Task)

    fun updateTask(task: Task)
}
