package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.uuid
import ru.yarsu.commands.RequestException
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.generateErrorBody

class DeleteUserHandler(
    private val userStorage: IUserStorage,
    private val categoryStorage: ICategoryStorage,
    private val taskStorage: ITaskStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val uuidLens = Path.uuid().of("user-id")
        val userUUID = uuidLens(request)
        val user = userStorage.getById(userUUID)

        if (user == null) {
            throw RequestException(
                generateErrorBody("Пользователь не найден", jsonMapper().createObjectNode().put("UserId", "$userUUID")),
                Status.NOT_FOUND,
            )
        }
        TODO()
    }
}
