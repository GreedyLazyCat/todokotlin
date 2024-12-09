package ru.yarsu.data.storage

import ru.yarsu.data.model.Category
import java.util.UUID

class CategoryStorage(
    val initCategories: List<Category>,
) : ICategoryStorage {
    private val categories: List<Category> = initCategories

    override fun getById(id: UUID): Category? = categories.find { it.id == id }
}
