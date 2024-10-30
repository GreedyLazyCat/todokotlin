package ru.yarsu.handlers

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.TaskStorage
import ru.yarsu.getPaginatedList

class ListTasksHandler(
    private val storage: TaskStorage,
) : HttpHandler {
    private val recordsPerPageValues = listOf(5, 10, 20, 50)

    fun tasksToString(tasks: List<Task>): String {
        val mapper = jacksonObjectMapper()
        mapper.registerModules(JavaTimeModule())
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val array = mapper.createArrayNode()
        for (task in tasks) {
            val node = mapper.createObjectNode()
            node.put("Id", "${task.id}")
            node.put("Title", "${task.title}")
            node.put("IsClose", "${task.isClose}")
            array.add(node)
        }
        return mapper.writeValueAsString(array)
    }

    fun generateErrorBody(text: String): String {
        val mapper = jacksonObjectMapper()
        val node = mapper.createObjectNode()
        node.put("error", text)
        return mapper.writeValueAsString(node)
    }

    fun validatePage(
        page: Int,
        recordsPerPage: Int,
    ) {
        if (page <= 0) {
            throw IllegalArgumentException("Ожидалось значение страницы >= 1, передано $page")
        }
        if (recordsPerPage <= 0) {
            throw IllegalArgumentException(
                "Ожидалось положительное значение количества элементов на странице>= 1, передано $recordsPerPage",
            )
        }
    }

    fun validateRecordsPerPage(recordsPerPage: Int) {
        if (!recordsPerPageValues.contains(recordsPerPage)) {
            throw IllegalArgumentException("Ожидалось одно из значений $recordsPerPageValues, передано $recordsPerPage")
        }
    }

    override fun invoke(request: Request): Response {
        try {
            val queryParams = request.uri.queries()
            val page = (queryParams.findSingle("page") ?: "1").toInt()
            val recordsPerPage = (queryParams.findSingle("records-per-page") ?: "10").toInt()
            val sortedTasks = storage.sortedWith(compareBy(Task::registrationDateTime, Task::id))

            validateRecordsPerPage(recordsPerPage)
            validatePage(page, recordsPerPage)

            val tasks = getPaginatedList(page, recordsPerPage, sortedTasks)

            val body = tasksToString(tasks)

            return Response(Status.OK).header("Content-type", "application/json").body(body)
        } catch (e: NumberFormatException) {
            return Response(Status.BAD_REQUEST)
                .header("Content-type", "application/json")
                .body(generateErrorBody("Ожидалось натуральное число в параметре page"))
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .header("Content-type", "application/json")
                .body(generateErrorBody(e.message ?: ""))
        }
    }
}
