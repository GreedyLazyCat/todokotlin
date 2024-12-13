package ru.yarsu.enum

import ru.yarsu.data.model.task.NoSuchTaskTypeException

enum class Color(
    val colorValues: List<String>,
) {
    BLACK(listOf("BLACK")),
    WHITE(listOf("WHITE")),
    RED(listOf("RED")),
    GREEN(listOf("GREEN")),
    BLUE(listOf("BLUE")),
    YELLOW(listOf("YELLOW")),
    CYAN(listOf("CYAN")),
    MAGENTA(listOf("MAGENTA")),
    SILVER(listOf("SILVER")),
    GRAY(listOf("GRAY")),
    MAROON(listOf("MAROON")),
    OLIVE(listOf("OLIVE")),
    DARKGREEN(listOf("DARKGREEN")),
    PURPLE(listOf("PUPRLE")),
    TEAL(listOf("TEAL")),
    ;

    companion object {
        fun getByValue(byValue: String): Color {
            for (type in Color.entries) {
                if (type.colorValues.contains(byValue)) {
                    return type
                }
            }
            throw NoSuchTaskTypeException(byValue)
        }
    }

    override fun toString(): String = colorValues.first()
}
