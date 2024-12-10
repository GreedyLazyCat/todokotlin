package ru.yarsu.data.factory

import com.fasterxml.jackson.databind.JsonNode
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.TaskImportanceType
import java.time.LocalDateTime
import java.util.UUID

class TasksFactory {
    fun createTaskFromStringList(list: List<String>): Task? {
//        println(list[0])
        try {
            return Task(
                id = UUID.fromString(list[0]),
                title = list[1],
                registrationDateTime = LocalDateTime.parse(list[2]),
                startDateTime = LocalDateTime.parse(list[3]),
                endDateTime = if (list[4] == "") null else LocalDateTime.parse(list[4]),
                importance = TaskImportanceType.getByValue(list[5].trim()),
                urgency = list[6].toBoolean(),
                percentage = list[7].toInt(),
                description = list[8],
                author = UUID.fromString(list[9]),
                category = UUID.fromString(list[10]),
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun createTaskFromJson(jsonNode: JsonNode): Task {
        val newId = UUID.randomUUID()
        return Task(
            id = newId,
            title = jsonNode.get("Title").asText(),
            registrationDateTime =
                if (jsonNode.has(
                        "RegistrationDateTime",
                    )
                ) {
                    LocalDateTime.parse(jsonNode.get("RegistrationDateTime").asText())
                } else {
                    LocalDateTime.now()
                },
            endDateTime =
                if (jsonNode.has(
                        "EndDateTime",
                    )
                ) {
                    LocalDateTime.parse(jsonNode.get("EndDateTime").asText())
                } else {
                    null
                },
            startDateTime =
                if (jsonNode.has(
                        "StartDateTime",
                    )
                ) {
                    LocalDateTime.parse(jsonNode.get("RegistrationDateTime").asText())
                } else {
                    LocalDateTime.now()
                },
            importance = TaskImportanceType.VERY_HIGH,
            urgency = false,
            author = UUID.randomUUID(),
            category = UUID.randomUUID(),
            description = "",
            percentage = 10,
        )
    }
}
