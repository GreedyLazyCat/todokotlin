package ru.yarsu.data.storage

import ru.yarsu.data.model.Category
import java.util.UUID

interface ICategoryStorage {
    fun getById(id: UUID): Category?
}
