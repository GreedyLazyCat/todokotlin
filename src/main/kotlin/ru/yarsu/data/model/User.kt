package ru.yarsu.data.model.task

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class User(
    val id: UUID,
    val login: String,
    val registrationDateTime: LocalDateTime,
    val email: String,
) {
    fun toJsonNode(): ObjectNode {
        val mapper =
            jsonMapper()
        val node = mapper.createObjectNode()
        node.put("Id", "$id")
        node.put("Login", login)
        node.put("RegistrationDateTime", registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        node.put("Email", email)
        return node
    }
}
