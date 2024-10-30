package ru.yarsu.data.model.task

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import java.time.LocalDateTime
import java.util.UUID

/*
* очень низкий, низкий, обычный, высокий, очень высокий, критический
* */

enum class TaskImportanceType(val eValue: String){
    VERY_LOW("очень низкий"),
    LOW("низкий"),
    REGULAR("обычный"),
    HIGH("высокий"),
    VERY_HIGH("очень высокий"),
    CRITICAL("критический");
    companion object{
        fun getByValue(byValue: String): TaskImportanceType {
            for(type in TaskImportanceType.entries){
                if (type.eValue == byValue){
                    return type;
                }
            }
            throw NoSuchTaskTypeException(byValue);
        }
    }

    override fun toString(): String {
        return eValue
    }
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
    val author: UUID
    ) {
    val isClose: Boolean
        get() = this.percentage == 100

    fun toJson():String{
        val mapper = jacksonObjectMapper()
        mapper.registerModules(JavaTimeModule())
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        return mapper.writeValueAsString(this)
    }
}
