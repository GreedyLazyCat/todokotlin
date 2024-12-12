package ru.yarsu.handlers.v2

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.commands.RequestException
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.validateTaskBody
import java.util.*

class UpdateTaskHandler(
    private val categoryStorage: ICategoryStorage,
    private val userStorage: IUserStorage,
    private val taskStorage: ITaskStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val bodyString = request.bodyString()
        val jsonTask = validateTaskBody(bodyString, categoryStorage)
        val taskId = UUID.fromString(jsonTask.get("id").asText())
        val task = taskStorage.getById(taskId)
        if (task == null) {
            throw RequestException()
        }

        TODO()
    }
}
