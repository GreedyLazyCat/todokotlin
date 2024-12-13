package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.data.model.Category
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.IUserStorage

class ReadCategoriesHandler(
    private val categoryStorage: ICategoryStorage,
    private val userStorage: IUserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val mapper = jsonMapper()
        val arrayNode = mapper.createArrayNode()
        val sortedCategories = categoryStorage.sortedWith(compareBy(Category::description, Category::id))
        for (category in sortedCategories) {
            val categoryNode = category.toJsonNode()
            if (category.owner != null) {
                val categoryOwner = userStorage.getById(category.owner)
                if (categoryOwner != null) {
                    categoryNode.put("OwnerName", categoryOwner.login)
                } else {
                    categoryNode.put("OwnerName", "Общая")
                }
            }
            arrayNode.add(categoryNode)
        }
        return Response(Status.OK).body(mapper.writeValueAsString(arrayNode))
    }
}
