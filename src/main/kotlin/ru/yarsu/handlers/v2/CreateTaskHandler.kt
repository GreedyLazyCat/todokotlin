package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.validateTaskBody

class CreateTaskHandler(
    val taskStorage: ITaskStorage,
    val userStorage: IUserStorage,
    val categoryStorage: ICategoryStorage,
) : HttpHandler {
    fun getCreatedBodyString(createdId: String): String {
        val mapper = jsonMapper()
        val node = mapper.createObjectNode()

        node.put("Id", createdId)

        return mapper.writeValueAsString(node)
    }

    override fun invoke(request: Request): Response {
        val bodyString = request.bodyString()
        val jsonNode = validateTaskBody(bodyString, categoryStorage)
        val task = TasksFactory().createTaskFromJson(jsonNode)
        taskStorage.addTask(task)
        return Response(Status.CREATED).body(getCreatedBodyString(task.id.toString()))
    }
}
