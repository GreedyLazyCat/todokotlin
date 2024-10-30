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
    }

    fun validateRecordsPerPage(recordsPerPage: Int) {
        if (!recordsPerPageValues.contains(recordsPerPage)) {
            throw IllegalArgumentException("Ожидалось одно из значений $recordsPerPageValues, передано $recordsPerPage")
        }
    }

    fun getPageContents(
        page: Int,
        recordsPerPage: Int,
    ): String {
        val sortedTasks = storage.sortedWith(compareBy(Task::registrationDateTime, Task::id))
        val end = page * recordsPerPage
        val start = end - recordsPerPage
        if (start >= sortedTasks.size) {
            return "[ ]"
        }

        if ((start - 1) < sortedTasks.size && (end - 1) >= sortedTasks.size) {
            return tasksToString(sortedTasks.subList(start, sortedTasks.size))
        }

        return tasksToString(sortedTasks.subList(start, end))
    }

    override fun invoke(request: Request): Response {
        try {
            val queryParams = request.uri.queries()
            val page = (queryParams.findSingle("page") ?: "1").toInt()
            val recordsPerPage = (queryParams.findSingle("records-per-page") ?: "10").toInt()

            validateRecordsPerPage(recordsPerPage)
            validatePage(page, recordsPerPage)

            var body = getPageContents(page, recordsPerPage)

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
