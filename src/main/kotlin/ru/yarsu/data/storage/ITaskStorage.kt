package ru.yarsu.data.storage

import ru.yarsu.data.model.task.Task
import java.util.UUID
import kotlin.Comparator

interface ITaskStorage {
    fun sortedWith(comparator: Comparator<in Task>): List<Task>

    fun getById(id: UUID): Task?
}
