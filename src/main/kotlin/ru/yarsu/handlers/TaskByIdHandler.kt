package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import org.http4k.routing.path
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.User
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.UserStorage
import ru.yarsu.generateErrorBody
import java.util.UUID

class TaskByIdHandler(
    private val taskStorage: ITaskStorage,
    private val userStorage: UserStorage,
) : HttpHandler {
    private fun generateNotFound(
        taskId: String,
        errorMessage: String,
    ): String {
        val mapper = jacksonObjectMapper()
        val node = mapper.createObjectNode()
        node.put("task-id", taskId)
        node.put("error", errorMessage)

        return mapper.writeValueAsString(node)
    }

    private fun generateResponse(
        task: Task,
        author: User,
    ): String {
        val mapper = jacksonObjectMapper()
        val taskNode = task.toMapperNode()
        taskNode.put("AuthorEmail", author.email)
        return mapper.writeValueAsString(taskNode)
    }

    override fun invoke(request: Request): Response {
        val taskIdString = request.path("task-id").orEmpty()
        try {
            if (taskIdString.isEmpty()) {
                return Response(
                    Status.BAD_REQUEST,
                ).contentType(ContentType.APPLICATION_JSON).body(generateErrorBody("Обязательный параметр task-id отсутствует"))
            }

            val taskId = UUID.fromString(taskIdString)
            val task =
                taskStorage.getById(taskId)
                    ?: return Response(Status.NOT_FOUND)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(generateNotFound(taskIdString, "Задача не найдена"))

            val author =
                userStorage.getById(task.author)
                    ?: return Response(Status.NOT_FOUND)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(generateNotFound(taskIdString, "Автор задачи с id ${task.author} не найден"))

            return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(generateResponse(task, author))
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(
                    generateErrorBody(
                        "Некорректный идентификатор задачи. Для параметра task-id ожидается UUID, но получено значение «$taskIdString»",
                    ),
                )
        }
    }
}
