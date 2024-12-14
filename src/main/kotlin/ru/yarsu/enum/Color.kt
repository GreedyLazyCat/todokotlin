package ru.yarsu.enum

import ru.yarsu.data.model.task.NoSuchTaskTypeException

enum class Color(
    val colorValues: List<String>,
) {
    BLACK(listOf("BLACK", "Чёрный", "#000000")),
    WHITE(listOf("WHITE", "Белый", "#FFFFFF")),
    RED(listOf("RED", "Красный", "#FF00000")),
    GREEN(listOf("GREEN", "Зелёный", "#00FF00")),
    BLUE(listOf("BLUE", "Синий", "#0000FF")),
    YELLOW(listOf("YELLOW", "Желтый", "#FFFF00")),
    CYAN(listOf("CYAN", "Голубой", "#00FFFF")),
    MAGENTA(listOf("MAGENTA", "Пурпурный", "#FF00FF")),
    SILVER(listOf("SILVER", "Серебряный", "#C0C0C0")),
    GRAY(listOf("GRAY", "Серый", "#808080")),
    MAROON(listOf("MAROON", "Бордовый", "#800000")),
    OLIVE(listOf("OLIVE", "Оливковый", "#808000")),
    DARKGREEN(listOf("DARKGREEN", "Тёмно-зеленый", "#008000")),
    PURPLE(listOf("PURPLE", "Фиолетовый", "#800080")),
    TEAL(listOf("TEAL", "Бирюзовый", "#008080")),
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
