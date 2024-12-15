package ru.yarsu.data.factory

import ru.yarsu.data.model.Category
import ru.yarsu.enum.Color
import java.util.UUID

class CategoryFactory {
    fun createFromStringList(row: List<String>): Category? {
        try {
            return Category(
                id = UUID.fromString(row[0]),
                description = row[1],
                color = Color.getByValue(row[2]),
                owner = if (row.size == 4 && row[3] != "") UUID.fromString(row[3]) else null,
            )
        } catch (e: Exception) {
            return null
        }
    }
}
