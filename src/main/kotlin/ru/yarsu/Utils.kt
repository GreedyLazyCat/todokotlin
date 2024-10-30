package ru.yarsu

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun generateErrorBody(text: String): String {
    val mapper = jacksonObjectMapper()
    val node = mapper.createObjectNode()
    node.put("error", text)
    return mapper.writeValueAsString(node)
}
