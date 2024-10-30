package ru.yarsu.data.model.task

class NoSuchTaskTypeException(
    val value: String,
) : Exception() {
    override val message: String?
        get() = value
}
