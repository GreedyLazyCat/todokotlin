package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task
import java.util.UUID
import kotlin.Comparator

class TaskStorage(
    tasksInit: List<Task>,
) : ITaskStorage {
    private val tasks: List<Task> = tasksInit

    override fun sortedWith(comparator: Comparator<in Task>): List<Task> = tasks.sortedWith(comparator)

    override fun getById(id: UUID): Task? = tasks.find { it.id == id }

    override fun filter(func: (Task) -> Boolean): List<Task> = tasks.filter(func)
}
