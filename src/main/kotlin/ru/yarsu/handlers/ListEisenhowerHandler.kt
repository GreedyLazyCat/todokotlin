package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import ru.yarsu.commands.CommandException
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.getPaginatedList

class ListEisenhowerHandler(
    private val storage: ITaskStorage,
) : HttpHandler {
    val allowedValues = listOf("true", "false")

    fun validateQueries(
        importantString: String?,
        urgentString: String?,
    ) {
        if (importantString == null && urgentString == null) {
            throw CommandException("There must be either an 'important' or 'urgent' key")
        }
        if (importantString == "" || urgentString == "") {
            throw CommandException("'important' or 'urgent' parameters are empty")
        }
        if (!allowedValues.contains(importantString) || allowedValues.contains(urgentString)) {
            throw CommandException("'important' or 'urgent' must be boolean")
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
        val page = (queryParams.findSingle("page") ?: "1").toInt()
        val recordsPerPage = (queryParams.findSingle("records-per-page") ?: "10").toInt()
        val importantString = queryParams.findSingle("important")
        val urgentString = queryParams.findSingle("urgent")

        validateQueries(importantString, urgentString)

        var filteredTasks =
            sortedTasks.filter {
                val important =
                    if (importantString == null) true else (it.importance.ordinal > 2 == importantString.toBoolean())
                var urgent = if (urgentString == null) true else (it.urgency == urgentString.toBoolean())
                important && urgent
            }

        filteredTasks = getPaginatedList(page, recordsPerPage, filteredTasks)

        return Response(Status.OK).header("Content-type", "application/json").body(generateResponseBody(filteredTasks))
    }
}
