package ru.yarsu.handlers.v2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.uuid
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.TaskImportanceType
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.generateErrorBody
import ru.yarsu.validateTaskBody
import java.time.LocalDateTime
import java.util.UUID

class UpdateTaskHandler(
    private val categoryStorage: ICategoryStorage,
    private val userStorage: IUserStorage,
    private val taskStorage: ITaskStorage,
) : HttpHandler {
    fun editedTask(
        task: Task,
        jsonTask: JsonNode,
    ): Task {
        var endDateTime: LocalDateTime? = null
        if (!jsonTask.get("EndDateTime").isNull) {
            endDateTime = LocalDateTime.parse(jsonTask.get("EndDateTime").asText())
        }

        return Task(
            id = task.id,
            title = if (jsonTask.has("Title")) jsonTask.get("Title").asText() else task.title,
            registrationDateTime =
                if (jsonTask.has(
                        "RegistrationDateTime",
                    )
                ) {
                    LocalDateTime.parse(jsonTask.get("RegistrationDateTime").asText())
                } else {
                    task.registrationDateTime
                },
            startDateTime =
                if (jsonTask.has(
                        "StartDateTime",
                    )
                ) {
                    LocalDateTime.parse(jsonTask.get("StartDateTime").asText())
                } else {
                    task.startDateTime
                },
            endDateTime =
                if (jsonTask.has(
                        "EndDateTime",
                    )
                ) {
                    endDateTime
                } else {
                    task.endDateTime
                },
            importance =
                if (jsonTask.has(
                        "Importance",
                    )
                ) {
                    TaskImportanceType.getByValue(jsonTask.get("Importance").asText())
                } else {
                    task.importance
                },
            urgency = if (jsonTask.has("Urgency")) (jsonTask.get("Urgency").asBoolean()) else task.urgency,
            percentage = if (jsonTask.has("Percentage")) jsonTask.get("Percentage").asInt() else task.percentage,
            description = if (jsonTask.has("Description")) jsonTask.get("Description").asText() else task.description,
            author = if (jsonTask.has("Author")) UUID.fromString(jsonTask.get("Author").asText()) else task.author,
            category = if (jsonTask.has("Category")) UUID.fromString(jsonTask.get("Category").asText()) else task.category,
        )
    }

    override fun invoke(request: Request): Response {
        val bodyString = request.bodyString()
        val jsonTask = validateTaskBody(bodyString, categoryStorage)
        val uuidLens = Path.uuid().of("task-id")
        val taskId = uuidLens(request)
        val task =
            taskStorage.getById(taskId)
                ?: throw RequestException(
                    generateErrorBody("Задача не найдена", jacksonObjectMapper().createObjectNode().put("Value", "$taskId")),
                )
        val editedTask = editedTask(task, jsonTask)
        taskStorage.updateTask(editedTask)
        return Response(Status.NO_CONTENT)
    }
}
