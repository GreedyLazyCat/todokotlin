package ru.yarsu.data.factory

import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.TaskImportanceType
import java.time.LocalDateTime
import java.util.*

class TasksFactory {
    fun createTaskFromString(
        id: String,
        title: String,
        registrationDateTime: String,
        startDateTime: String,
        endDateTime: String,
        importance: String,
        urgency: String,
        percentage: String,
        description: String
        ): Task?{

        return Task(
            id = UUID.fromString(id),
            title = title,
            registrationDateTime = LocalDateTime.parse(registrationDateTime),
            startDateTime = LocalDateTime.parse(startDateTime),
            endDateTime = if(endDateTime == "") null else LocalDateTime.parse(endDateTime),
            importance = TaskImportanceType.valueOf(importance),
            urgency = urgency.toBoolean(),
            percentage = percentage.toInt(),
            description = description)
    }

    fun createTaskFromStringList(
        list: List<String>
    ): Task?{
//        println(list[0])
       try {
           return Task(
               id = UUID.fromString(list[0]),
               title = list[1],
               registrationDateTime = LocalDateTime.parse(list[2]),
               startDateTime = LocalDateTime.parse(list[3]),
               endDateTime = if(list[4] == "") null else LocalDateTime.parse(list[4]),
               importance = TaskImportanceType.getByValue(list[5].trim()),
               urgency = list[6].toBoolean(),
               percentage = list[7].toInt(),
               description = list[8])
       }
       catch (e: Exception){
           return null
       }
    }
}
