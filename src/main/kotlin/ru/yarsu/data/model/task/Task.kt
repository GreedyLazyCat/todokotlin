package ru.yarsu.data.model.task

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/*
* очень низкий, низкий, обычный, высокий, очень высокий, критический
* */

enum class TaskImportanceType(
    val eValue: String,
) {
    VERY_LOW("очень низкий"),
    LOW("низкий"),
    REGULAR("обычный"),
    HIGH("высокий"),
    VERY_HIGH("очень высокий"),
    CRITICAL("критический"),
    ;

    companion object {
        fun getByValue(byValue: String): TaskImportanceType {
            for (type in TaskImportanceType.entries) {
                if (type.eValue == byValue) {
                    return type
                }
            }
            throw NoSuchTaskTypeException(byValue)
        }
    }

    override fun toString(): String = eValue
}

data class Task(
    val id: UUID,
    val title: String,
    val registrationDateTime: LocalDateTime,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime? = null,
    val importance: TaskImportanceType,
    val urgency: Boolean,
    val percentage: Int,
    val description: String,
    val author: UUID,
) {
    val isClosed: Boolean
        get() = this.percentage == 100

    fun toJson(): String {
        val mapper = jacksonObjectMapper()
        mapper.registerModules(JavaTimeModule())
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        return mapper.writeValueAsString(this)
    }

    fun toMapperNode(): ObjectNode {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()

        node.put("Id", id.toString())
        node.put("Title", title)
        node.put("RegistrationDateTime", registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        node.put("StartDateTime", startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        node.put("EndDateTime", endDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        node.put("Importance", importance.eValue)
        node.put("Urgency", urgency)
        node.put("Percentage", percentage)
        node.put("Description", description)
        node.put("Author", author.toString())

        return node
    }
}
