package ru.yarsu.commands

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import ru.yarsu.data.model.task.Task
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*

/**
 * Rem:
 *  Возможно слишком громоздкая идея.
 *  Просто не хотелось писать выполнение каждой команды в одной функции
 *  и огромном switch case.
 *  Нет, я все-таки переборщил, можно было просто вызывать функции
 */
class CommandExecutor {
    private val handlers: MutableList<(BaseCommand, List<Task>) -> Unit> = mutableListOf();

    init {
        registerCommandHandler(::showCommandHandler)
        registerCommandHandler(::listCommandHandler)
        registerCommandHandler(::listImportanceHandler)
        registerCommandHandler(::listTimeHandler)
        registerCommandHandler(::statsCommandHandler)
        registerCommandHandler(::findCommandHandler)
    }

    private fun getOutputGenerator(): JsonGenerator{
        val factory: JsonFactory = JsonFactoryBuilder().build()
        val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
        return outputGenerator
    }

    private fun getRussianDayOfWeek(dayOfWeek: DayOfWeek): String?{
        return when(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)){
            "Monday" -> "Понедельник"
            "Tuesday" -> "Вторник"
            "Wednesday" -> "Среда"
            "Thursday" -> "Четверг"
            "Friday" -> "Пятница"
            "Saturday" -> "Суббота"
            "Sunday" -> "Воскресенье"
            else ->{
                null
            }
        }
    }

    private fun findCommandHandler(command: BaseCommand, tasks: List<Task>){
        if(command is FindCommand){
            val text = command.text ?: throw CommandException()
            val textTrimmed = text.trim().lowercase()
            val filteredTasks = tasks.filter { it.title.lowercase().contains(textTrimmed) || it.description.lowercase().contains(textTrimmed)}
            val sortedTasks = filteredTasks.sortedWith(compareBy(Task::registrationDateTime, Task::id))

            with(getOutputGenerator()) {
                writeStartObject()
                writeFieldName("text")
                writeString(text.trim())
                writeFieldName("tasks")
                writeStartArray()
                for(task in sortedTasks){
                    writeStartObject()
                    writeFieldName("Id")
                    writeString(task.id.toString())

                    writeFieldName("Title")
                    writeString(task.title)

                    writeFieldName("RegistrationDateTime")
                    writeString(task.registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

                    writeFieldName("StartDateTime")
                    writeString(task.startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

                    writeFieldName("EndDateTime")
                    if(task.endDateTime != null){
                        writeString(task.endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    }
                    else{
                        writeObject(null)
                    }

                    writeFieldName("Importance")
                    writeString(task.importance.eValue)

                    writeFieldName("Urgency")
                    writeBoolean(task.urgency)

                    writeFieldName("Percentage")
                    writeNumber(task.percentage)

                    writeFieldName("Description")
                    writeString(task.description)

                    writeFieldName("IsClosed")
                    writeBoolean(task.percentage == 100)

                    writeEndObject()
                }
                writeEndArray()
                writeEndObject()
                close()
            }
        }
    }

    private fun listCommandHandler(command: BaseCommand, tasks: List<Task>){
        if (command is ListCommand){
            val sortedTasks = tasks.sortedWith(compareBy(Task::registrationDateTime, Task::id))


            with(getOutputGenerator()) {
                writeStartObject()
                writeFieldName("tasks")
                writeStartArray()
                for(task in sortedTasks){
                    writeStartObject()
                    writeFieldName("Id")
                    writeString(task.id.toString())
                    writeFieldName("Title")
                    writeString(task.title)
                    writeFieldName("IsClosed")
                    writeBoolean(task.percentage == 100)
                    writeEndObject()
                }
                writeEndArray()
                writeEndObject()
                close()
            }
        }
    }

    private fun showCommandHandler(command: BaseCommand, tasks: List<Task>){
        if (command is ShowCommand){
            val task = tasks.firstOrNull { it.id.toString() == command.taskId }
            if(task != null){
                with(getOutputGenerator()) {
                    writeStartObject()
                        writeFieldName("task-id")
                            writeString(task.id.toString())
                        writeFieldName("task")
                            writeStartObject()
                                writeFieldName("Id")
                                writeString(task.id.toString())

                                writeFieldName("Title")
                                writeString(task.title)

                                writeFieldName("RegistrationDateTime")
                                writeString(task.registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

                                writeFieldName("StartDateTime")
                                writeString(task.startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

                                writeFieldName("EndDateTime")
                                if(task.endDateTime != null){
                                    writeString(task.endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                }
                                else{
                                    writeObject(null)
                                }

                                writeFieldName("Importance")
                                writeString(task.importance.eValue)

                                writeFieldName("Urgency")
                                writeBoolean(task.urgency)

                                writeFieldName("Percentage")
                                writeNumber(task.percentage)

                                writeFieldName("Description")
                                writeString(task.description)

                                writeFieldName("IsClosed")
                                writeBoolean(task.percentage == 100)

                            writeEndObject()
                    writeEndObject()
                    close()
                }
            }
            else{
                throw CommandException("There is no task with id '${command.taskId}'")
            }
        }
    }

    private fun listImportanceHandler(command: BaseCommand, tasks: List<Task>){
        if(command is ListImportanceCommands){
            val sortedTasks = tasks.sortedWith(compareBy(Task::registrationDateTime, Task::id))
            if (command.important == null && command.urgent == null)
                throw CommandException("There must be either an 'important' or 'urgent' key")
            if(command.important == "" || command.urgent == "")
                throw CommandException("'important' or 'urgent' parameters are empty")

            val filteredTasks = sortedTasks.filter {
                    val important = if (command.important == null) true else (it.importance.ordinal > 2 == command.important.toBoolean())
                    var urgent = if(command.urgent == null) true else (it.urgency == command.urgent.toBoolean())
                    important && urgent
            }

            with(getOutputGenerator()) {
                writeStartObject()
                if(command.important != null){
                    writeFieldName("important")
                    writeBoolean(command.important.toBoolean())
                }

                if(command.urgent != null){
                    writeFieldName("urgent")
                    writeBoolean(command.urgent.toBoolean())
                }
                writeFieldName("tasks")
                writeStartArray()
                for(task in filteredTasks){
                    writeStartObject()
                    writeFieldName("Id")
                    writeString(task.id.toString())

                    writeFieldName("Title")
                    writeString(task.title)

                    writeFieldName("Importance")
                    writeString(task.importance.eValue)

                    writeFieldName("Urgency")
                    writeBoolean(task.urgency)

                    writeFieldName("Percentage")
                    writeNumber(task.percentage)

                    writeEndObject()
                }
                writeEndArray()
                writeEndObject()
                close()
            }
        }
    }

    private fun listTimeHandler(command: BaseCommand, tasks: List<Task>){
        if(command is ListTimeCommand){
            try {
                val time = LocalDateTime.parse(command.time)
                val sortedTasks = tasks.sortedWith(compareByDescending<Task> {it.importance }.thenByDescending{ it.urgency }.thenBy { it.registrationDateTime }.thenBy { it.id })
                val filteredTasks = sortedTasks.filter { it.percentage < 100 && it.startDateTime.isBefore(time)}

                with(getOutputGenerator()) {
                    writeStartObject()
                    writeFieldName("time")
                    writeString(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    writeFieldName("tasks")
                    writeStartArray()
                    for(task in filteredTasks){
                        writeStartObject()
                        writeFieldName("Id")
                        writeString(task.id.toString())

                        writeFieldName("Title")
                        writeString(task.title)

                        writeFieldName("Importance")
                        writeString(task.importance.eValue)

                        writeFieldName("Urgency")
                        writeBoolean(task.urgency)

                        writeFieldName("Percentage")
                        writeNumber(task.percentage)

                        writeEndObject()
                    }
                    writeEndArray()
                    writeEndObject()
                    close()
                }
            }
            catch (e: DateTimeParseException){
                throw CommandException("Error parsing date occurred: ${e.message}")
            }
        }
    }

    private fun String.dayOfWeekIndex(): Int{
        return when(this){
            "Понедельник" -> 0
            "Вторник" -> 1
            "Среда" -> 2
            "Четверг" -> 3
            "Пятница" -> 4
            "Суббота" -> 5
            "Воскресенье" -> 6
            else -> 0
        }
    }

    private fun statsCommandHandler(command: BaseCommand, tasks: List<Task>){
        if(command is StatsCommand){
            val results: MutableMap<String, Int> = mutableMapOf()
            var fieldName = ""
            fieldName = when(command.byDate){
                "registration" ->{
                    "statisticByRegistrationDateTime"
                }
                "start" ->{
                    "statisticByStartDateTime"
                }

                "end" ->{
                    "statisticByEndDateTime"
                }

                else ->{
                    throw CommandException("There is no such value of --by-date key as '${command.byDate}'")
                }
            }
            for (task in tasks){
                var dateTime:LocalDateTime? = null
                when(command.byDate){
                    "registration" ->{
                       dateTime = task.registrationDateTime
                    }
                    "start" ->{
                        dateTime = task.startDateTime
                    }
                    "end" ->{
                        if(task.endDateTime == null){
                            if (results.containsKey("Не заполнено")){
                                results.computeIfPresent("Не заполнено") { _, v -> v + 1 }
                            }
                            else{
                                results["Не заполнено"] = 1
                            }
                        }
                        else{
                           dateTime = task.endDateTime
                       }

                    }

                }
                val key = getRussianDayOfWeek((dateTime ?: continue).dayOfWeek) ?: continue
                if (results.containsKey(key)){
                    results.computeIfPresent(key) { _, v -> v + 1 }
                }
                else{
                    results[key] = 1
                }

            }
            val sortedKeys = results.keys.sortedWith(compareBy{it.dayOfWeekIndex()})
            with(getOutputGenerator()) {
                writeStartObject()
                writeFieldName(fieldName)
                writeStartArray()
                for(key in sortedKeys){
                    if(key == "Не заполнено")
                        continue
                    writeStartObject()
                        writeFieldName(key)
                        writeNumber(results[key] ?: throw CommandException("Map value is null"))
                    writeEndObject()
                }
                if(command.byDate == "end" && results.containsKey("Не заполнено")){
                    writeStartObject()
                        writeFieldName("Не заполнено")
                        writeNumber(results["Не заполнено"] ?: 1)
                    writeEndObject()
                }
                writeEndArray()
                writeEndObject()
                close()
            }

        }
    }

    private fun registerCommandHandler(executor: (BaseCommand, List<Task>) -> Unit){
        handlers.add(executor)
    }

    fun executeCommand(command: BaseCommand, tasks: List<Task>){
        for(handler in handlers){
            handler(command, tasks)
        }
    }
}
