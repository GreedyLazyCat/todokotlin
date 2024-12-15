package ru.yarsu.handlers.v2

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.LensFailure
import org.http4k.lens.Path
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.string
import org.http4k.lens.uuid
import org.http4k.lens.webForm
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.Category
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.ITaskStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.generateErrorBody
import ru.yarsu.tryParseUUID
import java.util.UUID

class UpdateCategoryHandler(
    private val categoryStorage: ICategoryStorage,
    private val userStorage: IUserStorage,
    private val taskStorage: ITaskStorage,
) : HttpHandler {
    fun validateForm(
        form: WebForm,
        descriptionField: BiDiLens<WebForm, String>,
        ownerField: BiDiLens<WebForm, String>,
    ) {
        val mapper = jsonMapper()
        val errorNode = mapper.createObjectNode()

        try {
            val description = descriptionField(form)
//            if (description.isEmpty()) {
//                val node = mapper.createObjectNode()
//                node.put("Value", "")
//                node.put("Error", "Отсутствует поле description")
//                errorNode.putIfAbsent("Description", node)
//            }
        } catch (e: LensFailure) {
            val node = mapper.createObjectNode()
            node.putIfAbsent("Value", null)
            node.put("Error", "Отсутствует поле description")
            errorNode.putIfAbsent("Description", node)
        }
        try {
            val owner = ownerField(form)
            if (owner.lowercase() != "null") {
                val ownerUUID = tryParseUUID(owner)
                if (ownerUUID == null) {
                    val node = mapper.createObjectNode()
                    node.put("Value", owner)
                    node.put("Error", "Некорректный формат Owner")
                    errorNode.putIfAbsent("Owner", node)
                } else if (userStorage.getById(ownerUUID) == null) {
                    val node = mapper.createObjectNode()
                    node.put("Value", owner)
                    node.put("Error", "Не существует owner с таким id")
                    errorNode.putIfAbsent("Owner", node)
                }
            }
        } catch (e: LensFailure) {
            val node = mapper.createObjectNode()
            node.putIfAbsent("Value", null)
            node.put("Error", "Отсутствует поле Owner")
            errorNode.putIfAbsent("Owner", node)
        }

        if (!errorNode.isEmpty) {
            throw RequestException(mapper.writeValueAsString(errorNode))
        }
    }

    fun updateTasks(
        category: Category,
        description: String,
        newOwnerString: String,
    ) {
        val mapper = jsonMapper()
        val newOwner = if (newOwnerString.lowercase() == "null") null else UUID.fromString(newOwnerString)
        if (category.owner == null && newOwner != null) {
            val anotherOwnerTasks = taskStorage.filter { it.author != newOwner && it.category == category.id }
            if (anotherOwnerTasks.isNotEmpty()) {
                val arrayNode = mapper.createArrayNode()
                for (task in anotherOwnerTasks) {
                    val node = mapper.createObjectNode()
                    node.put("TaskId", "${task.id}")
                    node.put("TaskTitle", "${task.title}")
                    node.put("Author", "${task.author}")
                    val author = userStorage.getById(task.author) ?: continue
                    node.put("AuthorLogin", author.login)
                    arrayNode.add(node)
                }
                throw RequestException(mapper.writeValueAsString(arrayNode), Status.FORBIDDEN)
            }
        }

        categoryStorage.update(category.copy(description = description, owner = newOwner))

        if (newOwner != null) {
            val prevOwnerTasks = taskStorage.filter { it.category == category.id }
            for (task in prevOwnerTasks) {
                taskStorage.updateTask(task.copy(author = newOwner))
            }
        }
    }

    override fun invoke(request: Request): Response {
        val categoryLens = Path.uuid().of("category-id")
        val categoryUUID = categoryLens(request)

        val descriptionField = FormField.string().required("Description")
        val ownerField = FormField.string().required("Owner")
        val formLens =
            Body
                .webForm(Validator.Feedback, descriptionField, ownerField)
                .toLens()

        val form = formLens(request)
        validateForm(form, descriptionField, ownerField)

        val category =
            categoryStorage.getById(categoryUUID)
                ?: throw RequestException(
                    generateErrorBody("Категория не найдена", jsonMapper().createObjectNode().put("CategoryId", "$categoryUUID")),
                    Status.NOT_FOUND,
                )

        val description = descriptionField(form)
        val ownerString = ownerField(form)
        updateTasks(category, description, ownerString)
        return Response(Status.NO_CONTENT)
    }
}
