package ru.yarsu.data.storage

import ru.yarsu.data.model.Category
import java.util.UUID

class CategoryStorage(
    initCategories: List<Category>,
) : ICategoryStorage {
    private val categories: MutableList<Category> = initCategories.toMutableList()

    override fun getById(id: UUID): Category? = categories.find { it.id == id }

    override fun sortedWith(comparator: Comparator<in Category>): List<Category> = categories.sortedWith(comparator)

    override fun filter(func: (Category) -> Boolean): List<Category> = categories.filter(func)

    override fun update(category: Category): Boolean {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index == -1) {
            return false
        }
        categories[index] = category
        return true
    }

    override fun getCategories(): List<Category> = categories.toList()
}
