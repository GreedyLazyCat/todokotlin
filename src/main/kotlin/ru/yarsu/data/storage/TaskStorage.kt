package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task
import java.util.UUID
import kotlin.Comparator

class TaskStorage(
    tasksInit: List<Task>,
) : ITaskStorage {
    private val tasks: MutableList<Task> = tasksInit.toMutableList()

    override fun sortedWith(comparator: Comparator<in Task>): List<Task> = tasks.sortedWith(comparator)

    override fun getById(id: UUID): Task? = tasks.find { it.id == id }

    override fun filter(func: (Task) -> Boolean): List<Task> = tasks.filter(func)

    override fun getTasks(): List<Task> = tasks.toList()

    override fun addTask(task: Task) {
        tasks.add(task)
//        TODO("write to csv")
    }

    override fun updateTask(task: Task) {
        val taskIndex = tasks.indexOfFirst { it.id == task.id }
        if (taskIndex != -1) {
            tasks[taskIndex] = task
        }
    }
}
