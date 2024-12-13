package ru.yarsu.data.storage

import ru.yarsu.data.model.Category
import java.util.UUID

class CategoryStorage(
    initCategories: List<Category>,
) : ICategoryStorage {
    private val categories: List<Category> = initCategories.toMutableList()

    override fun getById(id: UUID): Category? = categories.find { it.id == id }
}
