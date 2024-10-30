package ru.yarsu

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun generateErrorBody(text: String): String {
    val mapper = jacksonObjectMapper()
    val node = mapper.createObjectNode()
    node.put("error", text)
    return mapper.writeValueAsString(node)
}

fun <T> getPaginatedList(
    page: Int,
    elementsPerPage: Int,
    list: List<T>,
): List<T> {
    val end = page * elementsPerPage
    val start = end - elementsPerPage

    if (start >= list.size) {
        return listOf()
    }

    if ((start - 1) < list.size && (end - 1) >= list.size) {
        return list.subList(start, list.size)
    }

    return list.subList(start, end)
}
