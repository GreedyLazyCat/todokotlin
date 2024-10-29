package ru.yarsu

import com.beust.jcommander.JCommander
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import ru.yarsu.commands.*
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.model.task.Task
import kotlin.system.exitProcess
import java.util.*

fun parseTasks(filePath: String): List<Task>{
    System.setProperty("file.encoding", "UTF8")
    var skippedFirstRow = false
    var result: MutableList<Task> = mutableListOf()

    csvReader().open(filePath) {
        readAllAsSequence().forEach { row: List<String> ->
            val task: Task? = TasksFactory().createTaskFromStringList(row)
            if (task != null)
                result.add(task)
        }
    }

    return result
}

fun parseCommand(params: Array<String>): BaseCommand{
    val showCmd  = ShowCommand()
    val listCmd = ListCommand()
    val listImportanceCmd = ListImportanceCommands()
    val listTimeCmd = ListTimeCommand()
    val statsCmd  = StatsCommand()
    val findCmd = FindCommand()

    val commander: JCommander =
        JCommander
            .newBuilder()
            .addCommand("show", showCmd)
            .addCommand("list", listCmd)
            .addCommand("list-eisenhower", listImportanceCmd)
            .addCommand("list-time", listTimeCmd)
            .addCommand("statistic", statsCmd)
            .addCommand("find", findCmd)
            .build()

    commander.parse(*params)
    return when(commander.parsedCommand){
        "show" -> showCmd
        "list" -> listCmd
        "list-eisenhower" -> listImportanceCmd
        "list-time" -> listTimeCmd
        "statistic" -> statsCmd
        "find" -> findCmd
        else -> {
            throw CommandException("There is no such command or command is empty")
        }
    }
}

fun main(params: Array<String>) {
    try {
        //chage to params
        val command = parseCommand(params)
        val tasks = parseTasks(command.tasksFile ?: "")
        CommandExecutor().executeCommand(command, tasks)
    }
    catch (e: Exception){
        if(e.message != null) print("${e.message}")
        exitProcess(1)
    }
}
//FileNotFoundException, ParameterException, CSVFieldNumDifferentException, CommandException
