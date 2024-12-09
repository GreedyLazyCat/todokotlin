package ru.yarsu.handlers.v1

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.queries
import org.http4k.lens.contentType
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.TaskStorage
import ru.yarsu.generateErrorBody
import ru.yarsu.getPaginatedList
import ru.yarsu.validatePagination

class ListTasksHandler(
    private val storage: TaskStorage,
) : HttpHandler {
    fun tasksToString(tasks: List<Task>): String {
        val mapper = jacksonObjectMapper()
        mapper.registerModules(JavaTimeModule())
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val array = mapper.createArrayNode()
        for (task in tasks) {
            val node = mapper.createObjectNode()
            node.put("Id", "${task.id}")
            node.put("Title", "${task.title}")
            node.put("IsClosed", task.isClosed)
            array.add(node)
        }
        return mapper.writeValueAsString(array)
    }

    override fun invoke(request: Request): Response {
        try {
            val queryParams = request.uri.queries()
            val sortedTasks = storage.sortedWith(compareBy(Task::registrationDateTime, Task::id))

            val tasks = getPaginatedList(queryParams, sortedTasks, ::validatePagination)

            val body = tasksToString(tasks)

            return Response(Status.OK).body(body)
        } catch (e: NumberFormatException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(generateErrorBody("Ожидалось натуральное число в параметре page"))
        } catch (e: RequestException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(generateErrorBody(e.message ?: ""))
        }
    }
}
