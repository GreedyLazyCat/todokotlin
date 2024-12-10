package ru.yarsu.handlers.v2

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import ru.yarsu.commands.RequestException
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.generateErrorBody
import java.util.UUID

class CreateTaskHandler(
    val taskStorage: ITaskStorage,
    val userStorage: IUserStorage,
    val categoryStorage: ICategoryStorage,
) : HttpHandler {
    fun validateBody(bodyString: String): JsonNode {
        try {
            val jsonBody = bodyString.asJsonObject()
            println(jsonBody.has("Title"))
            if (!jsonBody.has("Title") || jsonBody.get("Title").asText().isEmpty()) {
                throw RequestException(generateErrorBody("Title field is required"))
            }
            if (!jsonBody.has("Author") ||
                jsonBody
                    .get("Author")
                    .asText()
                    .isEmpty()
            ) {
                throw RequestException(generateErrorBody("Author field is required"))
            }
            if (!jsonBody.has("Category") || jsonBody.get("Category").asText().isEmpty()) {
                throw RequestException(generateErrorBody("Category field is required"))
            }
            val authorUUID = UUID.fromString(jsonBody.get("Author").asText())
            val categoryUUID = UUID.fromString(jsonBody.get("Category").asText())
            val category = categoryStorage.getById(categoryUUID)
            if (category == null) {
                RequestException(generateErrorBody("No such category", "$categoryUUID"))
            }
            return jsonBody
        } catch (e: JsonParseException) {
            throw RequestException(generateErrorBody("Json parsing error", bodyString))
        }
    }

    fun getCreatedBodyString(createdId: String): String {
        val mapper = jsonMapper()
        val node = mapper.createObjectNode()
        node.put("Id", createdId)
        return mapper.writeValueAsString(node)
    }

    override fun invoke(request: Request): Response {
        val bodyString = request.bodyString()
        val jsonNode = validateBody(bodyString)
        val task = TasksFactory().createTaskFromJson(jsonNode)
        taskStorage.addTask(task)
        return Response(Status.CREATED).body(getCreatedBodyString(task.id.toString()))
    }
}
