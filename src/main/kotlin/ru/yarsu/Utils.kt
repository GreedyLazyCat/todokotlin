package ru.yarsu

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Parameters
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.findSingle
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.mapper
import org.http4k.lens.contentType
import ru.yarsu.commands.RequestException
import ru.yarsu.data.storage.ICategoryStorage
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

val elementsPerPageValues = listOf(5, 10, 20, 50)

fun validatedTaskBody(
    bodyString: String,
    categoryStorage: ICategoryStorage,
): JsonNode {
    try {
        val jsonBody = bodyString.asJsonObject()
        val mapper = jsonMapper()
        val errorNode = mapper.createObjectNode()

        validateField<LocalDateTime>(jsonBody, "RegistrationDateTime", errorNode, false) {
            try {
                LocalDateTime.parse(jsonBody.get("RegistrationDateTime").asText())
            } catch (e: DateTimeParseException) {
                null
            }
        }
        validateField(jsonBody, "Author", errorNode, true) {
            try {
                UUID.fromString(jsonBody.get("Author").asText())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        if (!errorNode.isEmpty) {
            throw RequestException(mapper.writeValueAsString(errorNode))
        }
        return jsonBody
    } catch (e: JsonParseException) {
        throw RequestException(generateErrorBody("Json parsing error", mapper.createObjectNode().put("Value", "$bodyString")))
    }
}

fun <T> validateField(
    jsonBody: JsonNode,
    field: String,
    errors: ObjectNode,
    required: Boolean,
    typeCaster: () -> T?,
) {
    val mapper = jsonMapper()
    val errorNode = mapper.createObjectNode()
    val node = jsonBody.get(field)
    if (required && (node == null || node.isNull || node.asText().isEmpty())) {
        errorNode.putIfAbsent("Value", node)
        errorNode.put("Error", "Это значение обязательно и не может быть пустым")
        errors.putIfAbsent(field, errorNode)
        return
    }
    if (node == null) {
        return
    }
    val casted = typeCaster()
    if (casted == null) {
        errorNode.putIfAbsent("Value", node)
        errorNode.put("Error", "Значение передано в некорректном формате")
        errors.putIfAbsent(field, errorNode)
    }
}

fun tryParseUUID(str: String): UUID? {
    try {
        return UUID.fromString(str)
    } catch (e: IllegalArgumentException) {
        return null
    }
}

fun requestExceptionFilter(): Filter =
    Filter { next: HttpHandler ->
        { request: Request ->
            try {
                next(request)
            } catch (requestException: RequestException) {
                Response(requestException.status)
                    .body("${requestException.message}")
            }
        }
    }

val jsonContentTypeFilter =
    Filter { next: HttpHandler ->
        { request ->
            val response = next(request)
            if (response.bodyString().isNotEmpty()) {
                response.contentType(ContentType.APPLICATION_JSON)
            } else {
                response
            }
        }
    }

fun generateErrorBody(
    text: String,
    args: JsonNode? = null,
): String {
    val mapper = jacksonObjectMapper()
    val node = mapper.createObjectNode()
    node.put("Error", text)
    if (args != null) {
        for (entry in args.fields()) {
            node.putIfAbsent(entry.key, entry.value)
        }
    }
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
        throw RequestException("Некорректное значение параметра page. Ожидается натуральное число, но получено $parsedPage")
    }
    if (elementsPerPage == null) {
        throw RequestException("Некорректное значение параметра page. Ожидается натуральное число, но получено $elementsPerPageParsed")
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

fun getRussianDayOfWeek(dayOfWeek: DayOfWeek): String? =
    when (dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)) {
        "Monday" -> "Понедельник"
        "Tuesday" -> "Вторник"
        "Wednesday" -> "Среда"
        "Thursday" -> "Четверг"
        "Friday" -> "Пятница"
        "Saturday" -> "Суббота"
        "Sunday" -> "Воскресенье"
        else -> {
            null
        }
    }

fun simpleMapStringToJsonString(inMap: Map<String, String>): String {
    val mapper = jsonMapper()
    val node = mapper.createObjectNode()
    for (key in inMap.keys) {
        node.put(key, inMap[key])
    }
    return mapper.writeValueAsString(node)
}
