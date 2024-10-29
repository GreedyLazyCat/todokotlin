package ru.yarsu.data.model.task

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
    val description: String
    ) {

}
