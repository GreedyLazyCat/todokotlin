package ru.yarsu.data.model.task

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val login: String,
    val registrationDateTime: LocalDateTime,
    val email: String,
)
