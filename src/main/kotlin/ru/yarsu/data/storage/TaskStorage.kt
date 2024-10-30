package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task



class TaskStorage(tasksInit: List<Task>):ITaskStorage {
    private val tasks: List<Task> = tasksInit;

    override fun sortedWith(comparator: Comparator<in Task>): List<Task> {
        return tasks.sortedWith(comparator)
    }

}
