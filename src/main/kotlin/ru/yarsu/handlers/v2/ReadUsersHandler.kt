package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.data.model.task.User
import ru.yarsu.data.storage.IUserStorage

class ReadUsersHandler(
    private val userStorage: IUserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val mapper = jsonMapper()
        val arrayNode = mapper.createArrayNode()
        val sortedUsers = userStorage.sortedWith(compareBy(User::login))
        for (user in sortedUsers) {
            val userNode = user.toJsonNode()
            arrayNode.add(userNode)
        }

        return Response(Status.OK).body(mapper.writeValueAsString(arrayNode))
    }
}
