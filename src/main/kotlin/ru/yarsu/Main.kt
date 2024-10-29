package ru.yarsu

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ser.Serializers.Base
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import ru.yarsu.commands.*
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.factory.UserFactory
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.User
import kotlin.system.exitProcess
import java.util.*

fun parseTasks(filePath: String): List<Task>{
    System.setProperty("file.encoding", "UTF8")
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

fun parseUsers(filePath: String): List<User>{
    System.setProperty("file.encoding", "UTF8")
    var result: MutableList<User> = mutableListOf()

    csvReader().open(filePath) {
        readAllAsSequence().forEach { row: List<String> ->
            val user: User? = UserFactory().createFromStringList(row)
            if (user != null)
                result.add(user)
        }
    }

    return result
}

fun parseCommand(params: Array<String>): BaseCommand{
    val baseCommand = BaseCommand()

    val commander: JCommander =
        JCommander
            .newBuilder()
            .addObject(baseCommand)
            .build()

    commander.parse(*params)
    return baseCommand
}

fun main(params: Array<String>) {
//    try {
        //chage to params
        val mockParams = arrayOf("--tasks-file=C:\\Uni\\WebApps\\ToDoListNew\\src\\main\\resources\\tasks.csv",
            "--users-file=C:\\Uni\\WebApps\\ToDoListNew\\src\\main\\resources\\users.csv ",
            "--port=9000")
        val command = parseCommand(mockParams)
        val tasks = parseTasks(command.tasksFile ?: "")
        val users = parseUsers(command.usersFile ?: "")
    println(tasks)
    println(users)
//        CommandExecutor().executeCommand(command, tasks)
//    }
//    catch (e: Exception){
//        if(e.message != null) print("${e.message}")
//        exitProcess(1)
//    }
}
//FileNotFoundException, ParameterException, CSVFieldNumDifferentException, CommandException
