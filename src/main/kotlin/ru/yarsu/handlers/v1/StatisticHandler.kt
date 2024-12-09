package ru.yarsu.handlers.v1

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.lens.contentType
import ru.yarsu.commands.RequestException
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.storage.TaskStorage
import ru.yarsu.generateErrorBody
import ru.yarsu.getRussianDayOfWeek
import java.time.LocalDateTime

class StatisticHandler(
    private val storage: TaskStorage,
) : HttpHandler {
    private fun validateByDateAndGetFieldName(byDate: String): String =
        when (byDate) {
            "registration" -> {
                "statisticByRegistrationDateTime"
            }
            "start" -> {
                "statisticByStartDateTime"
            }

            "end" -> {
                "statisticByEndDateTime"
            }
            else -> {
                throw RequestException(
                    "Значение by-date обязательно и должно быть равно одному из значений: registration, start, end. Передано $byDate",
                )
            }
        }

    private fun String.dayOfWeekIndex(): Int =
        when (this) {
            "Понедельник" -> 0
            "Вторник" -> 1
            "Среда" -> 2
            "Четверг" -> 3
            "Пятница" -> 4
            "Суббота" -> 5
            "Воскресенье" -> 6
            else -> 0
        }

    private fun genrateResponse(
        tasks: List<Task>,
        byDate: String,
        fieldName: String,
    ): String {
        val mapper = jacksonObjectMapper()
        val results: MutableMap<String, Int> = mutableMapOf()
        val resultNode = mapper.createObjectNode()
        val datesInfoNode = mapper.createObjectNode()

        for (task in tasks) {
            var dateTime: LocalDateTime? = null
            when (byDate) {
                "registration" -> {
                    dateTime = task.registrationDateTime
                }
                "start" -> {
                    dateTime = task.startDateTime
                }
                "end" -> {
                    if (task.endDateTime == null) {
                        if (results.containsKey("Не заполнено")) {
                            results.computeIfPresent("Не заполнено") { _, v -> v + 1 }
                        } else {
                            results["Не заполнено"] = 1
                        }
                    } else {
                        dateTime = task.endDateTime
                    }
                }
            }
            val key = getRussianDayOfWeek((dateTime ?: continue).dayOfWeek) ?: continue
            if (results.containsKey(key)) {
                results.computeIfPresent(key) { _, v -> v + 1 }
            } else {
                results[key] = 1
            }
        }
        val sortedKeys = results.keys.sortedWith(compareBy { it.dayOfWeekIndex() })
        for (key in sortedKeys) {
            if (key == "Не заполнено") {
                continue
            }
            datesInfoNode.put(key, results[key])
        }
        if (byDate == "end" && results.containsKey("Не заполнено")) {
            datesInfoNode.put("Не заполнено", results["Не заполнено"])
        }
        resultNode.putIfAbsent(fieldName, datesInfoNode)
        return mapper.writeValueAsString(resultNode)
    }

    override fun invoke(request: Request): Response {
        val queryParams = request.uri.queries()
        val byDate = queryParams.findSingle("by-date").orEmpty()
        try {
            val fieldName = validateByDateAndGetFieldName(byDate)

            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(genrateResponse(storage.getTasks(), byDate, fieldName))
        } catch (e: RequestException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(generateErrorBody(e.message.toString()))
        }
    }
}
