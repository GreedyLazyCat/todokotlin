package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task

interface ITaskStorage {
    fun sortedWith(comparator:Comparator<in Task>): List<Task>

}
