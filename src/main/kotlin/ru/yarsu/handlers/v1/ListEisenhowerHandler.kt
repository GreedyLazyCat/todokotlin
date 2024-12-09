package ru.yarsu.handlers.v1

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.lens.contentType
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.generateErrorBody
import ru.yarsu.getPaginatedList
import ru.yarsu.validatePagination

class ListEisenhowerHandler(
    private val storage: ITaskStorage,
) : HttpHandler {
    val allowedValues = listOf("true", "false")

    fun validateQueries(
        importantString: String?,
        urgentString: String?,
    ) {
        if (importantString == null && urgentString == null) {
            throw RequestException("В параметрах должны быть указаны хотя бы один из ключей 'important' или 'urgent'.")
        }
        if (importantString == "" || urgentString == "") {
            throw RequestException("Параметры 'important' и 'urgent' не должны быть пустыми.")
        }
        if (!allowedValues.contains(importantString ?: allowedValues[0]) || !allowedValues.contains(urgentString ?: allowedValues[0])) {
            throw RequestException("Параметры 'important' и 'urgent' должны быть булевыми.")
        }
    }

    fun generateResponseBody(tasks: List<Task>): String {
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()
        for (task in tasks) {
            val node = mapper.createObjectNode()
            node.put("Id", task.id.toString())
            node.put("Title", task.title)
            node.put("Importance", task.importance.eValue)
            node.put("Urgency", task.urgency)
            node.put("Percentage", task.percentage)
            arrayNode.add(node)
        }
        return mapper.writeValueAsString(arrayNode)
    }

    // TODO: validate pages
    override fun invoke(request: Request): Response {
        val sortedTasks = storage.sortedWith(compareBy(Task::registrationDateTime, Task::id))
        val queryParams = request.uri.queries()
        val importantString = queryParams.findSingle("important")
        val urgentString = queryParams.findSingle("urgent")

        try {
            validateQueries(importantString, urgentString)

            var filteredTasks =
                sortedTasks.filter {
                    val important =
                        if (importantString == null) true else (it.importance.ordinal > 2 == importantString.toBoolean())
                    var urgent = if (urgentString == null) true else (it.urgency == urgentString.toBoolean())
                    important && urgent
                }

            filteredTasks = getPaginatedList(queryParams, filteredTasks, ::validatePagination)

            return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(generateResponseBody(filteredTasks))
        } catch (e: RequestException) {
            return Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(generateErrorBody(e.message.toString()))
        }
    }
}
