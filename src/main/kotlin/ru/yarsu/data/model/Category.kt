package ru.yarsu.data.model

import ru.yarsu.enum.Color
import java.util.UUID

data class Category(
    val id: UUID,
    val description: String,
    val color: Color,
    val owner: UUID?,
)
