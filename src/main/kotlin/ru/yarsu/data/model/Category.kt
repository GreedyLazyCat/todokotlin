package ru.yarsu.data.model

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import ru.yarsu.enum.Color
import java.util.UUID

data class Category(
    val id: UUID,
    val description: String,
    val color: Color,
    val owner: UUID?,
) {
    fun toJsonNode(): ObjectNode {
        val mapper = jsonMapper()
        val node = mapper.createObjectNode()
        node.put("Id", "$id")
        node.put("Description", description)
        node.put("Color", color.colorValues.first())
        if (owner != null) {
            node.put("Owner", "$owner")
        }
        return node
    }
}
