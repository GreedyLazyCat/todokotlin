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
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class ListTimeHandler(
    private val storage: ITaskStorage,
) : HttpHandler {
    private fun validateAndParseDateTime(dateString: String?): LocalDateTime {
        if (dateString == null) {
            throw RequestException("Параметр time обязательный.")
        }
        try {
            return LocalDateTime.parse(dateString)
        } catch (e: DateTimeParseException) {
            throw RequestException("Не правильный формат time")
        }
    }

    private fun generateOkResponse(tasks: List<Task>): String {
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

    override fun invoke(request: Request): Response {
        val queryParams = request.uri.queries()
        val dateString = queryParams.findSingle("time")
        try {
            val date = validateAndParseDateTime(dateString)

            // sort
            var tasks =
                storage.sortedWith(
                    compareByDescending<Task> {
                        it.importance
                    }.thenByDescending { it.urgency }.thenBy { it.registrationDateTime }.thenBy { it.id },
                )

            // filter
            tasks = tasks.filter { it.percentage < 100 && it.startDateTime.isBefore(date) }

            // paginate
            tasks = getPaginatedList(queryParams, tasks, ::validatePagination)

            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(generateOkResponse(tasks))
        } catch (e: RequestException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(generateErrorBody(e.message.toString()))
        }
    }
}
