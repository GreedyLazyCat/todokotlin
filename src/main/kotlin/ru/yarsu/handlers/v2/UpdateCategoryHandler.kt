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
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.string
import org.http4k.lens.webForm
import ru.yarsu.commands.RequestException
import ru.yarsu.data.storage.ICategoryStorage
import ru.yarsu.data.storage.IUserStorage
import ru.yarsu.tryParseUUID

class UpdateCategoryHandler(
    private val categoryStorage: ICategoryStorage,
    private val userStorage: IUserStorage,
) : HttpHandler {
    fun validateForm(
        form: WebForm,
        descriptionField: BiDiLens<WebForm, String>,
        ownerField: BiDiLens<WebForm, String>,
    ) {
        val mapper = jsonMapper()
        val errorNode = mapper.createObjectNode()

        try {
            descriptionField(form)
        } catch (e: LensFailure) {
            val node = mapper.createObjectNode()
            node.putIfAbsent("Value", null)
            node.put("Error", "Отсутствует поле description")
            errorNode.putIfAbsent("Description", node)
        }
        try {
            val owner = ownerField(form)
            val ownerUUID = tryParseUUID(owner)
            if (ownerUUID == null) {
                val node = mapper.createObjectNode()
                node.put("Value", owner)
                node.put("Error", "Некорректный формат Owner")
                errorNode.putIfAbsent("Owner", node)
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

    override fun invoke(request: Request): Response {
        val descriptionField = FormField.string().required("Description")
        val ownerField = FormField.string().required("Owner")
        val formLens =
            Body
                .webForm(Validator.Feedback, descriptionField, ownerField)
                .toLens()
        val form = formLens(request)
        validateForm(form, descriptionField, ownerField)

        return Response(Status.NO_CONTENT)
    }
}
