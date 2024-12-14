package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.uuid
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.Category
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.generateErrorBody

class DeleteUserHandler(
    private val userStorage: IUserStorage,
    private val categoryStorage: ICategoryStorage,
    private val taskStorage: ITaskStorage,
) : HttpHandler {
    fun generateForbiddenResponse(
        userCategories: List<Category>,
        userTasks: List<Task>,
    ): String {
        val mapper = jsonMapper()
        val resultNode = mapper.createObjectNode()

        var arrayNode = mapper.createArrayNode()
        for (category in userCategories) {
            val categoryNode = mapper.createObjectNode()
            categoryNode.put("Id", "${category.id}")
            categoryNode.put("Description", "${category.description}")
            arrayNode.add(categoryNode)
        }
        resultNode.putIfAbsent("Categories", arrayNode)

        arrayNode = mapper.createArrayNode()
        for (task in userTasks) {
            val categoryNode = mapper.createObjectNode()
            categoryNode.put("Id", "${task.id}")
            categoryNode.put("Title", "${task.title}")
            arrayNode.add(categoryNode)
        }
        resultNode.putIfAbsent("Tasks", arrayNode)

        return mapper.writeValueAsString(resultNode)
    }

    override fun invoke(request: Request): Response {
        val uuidLens = Path.uuid().of("user-id")
        val userUUID = uuidLens(request)
        val userCategories = categoryStorage.filter { it.owner == userUUID }.sortedWith(compareBy(Category::id))
        val userTasks = taskStorage.filter { it.author == userUUID }.sortedWith(compareBy(Task::id))

        if (userCategories.isNotEmpty() || userTasks.isNotEmpty()) {
            throw RequestException(generateForbiddenResponse(userCategories, userTasks), Status.FORBIDDEN)
        }

        if (userStorage.getById(userUUID) == null) {
            throw RequestException(
                generateErrorBody("Пользователь не найден", jsonMapper().createObjectNode().put("UserId", "$userUUID")),
                Status.NOT_FOUND,
            )
        }
        userStorage.deleteUser(userUUID)
        return Response(Status.NO_CONTENT)
    }
}
