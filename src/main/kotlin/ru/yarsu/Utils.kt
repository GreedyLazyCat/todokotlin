package ru.yarsu

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Parameters
import org.http4k.core.findSingle
import ru.yarsu.commands.RequestException

val elementsPerPageValues = listOf(5, 10, 20, 50)

fun generateErrorBody(text: String): String {
    val mapper = jacksonObjectMapper()
    val node = mapper.createObjectNode()
    node.put("error", text)
    return mapper.writeValueAsString(node)
}

fun <T> getPaginatedList(
    queryParams: Parameters,
    list: List<T>,
    queryValidator: (Parameters) -> Unit,
): List<T> {
    queryValidator(queryParams)
    val page = (queryParams.findSingle("page") ?: "1").toInt()
    val elementsPerPage = (queryParams.findSingle("records-per-page") ?: "10").toInt()
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

fun validatePagination(queryParams: Parameters) {
    val parsedPage = queryParams.findSingle("page")
    val elementsPerPageParsed = queryParams.findSingle("records-per-page")

    val page = (parsedPage ?: "1").toIntOrNull()
    val elementsPerPage = (elementsPerPageParsed ?: "10").toIntOrNull()

    if (page == null) {
        throw RequestException("Ожидалось целочисленное значение page, передано $parsedPage")
    }
    if (elementsPerPage == null) {
        throw RequestException("Ожидалось целочисленное значение records-per-page, передано $elementsPerPageParsed")
    }
    if (page <= 0) {
        throw RequestException("Ожидалось значение страницы >= 1, передано $page")
    }
    if (elementsPerPage <= 0) {
        throw RequestException(
            "Ожидалось положительное значение количества элементов на странице >= 1, передано $elementsPerPage",
        )
    }
    if (!elementsPerPageValues.contains(elementsPerPage)) {
        throw RequestException("Ожидалось одно из значений $elementsPerPageValues, передано $elementsPerPage")
    }
}

fun validateElementsPerPage(recordsPerPage: Int) {
    if (!elementsPerPageValues.contains(recordsPerPage)) {
        throw RequestException("Ожидалось одно из значений $elementsPerPageValues, передано $recordsPerPage")
    }
}
