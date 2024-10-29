package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import ru.yarsu.commands.*
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.factory.UserFactory
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.User
import org.http4k.server.asServer
import java.io.File
import kotlin.system.exitProcess

fun parseTasks(filePath: String): List<Task>{
    System.setProperty("file.encoding", "UTF8")
    var result: MutableList<Task> = mutableListOf()
    val file: File = File(filePath)
    println(file.absolutePath)

    csvReader().open(file) {
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
    val file: File = File(filePath)

    csvReader().open(file) {
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

val app = routes(
    "/ping" bind Method.GET to {
        Response(Status.OK).body("Приложение запущено")
    }
)

fun main(params: Array<String>) {
    try{
        val mockParams = arrayOf(
            "--tasks-file=src/main/resources/tasks.csv",
            "--users-file=src/main/resources/users.csv",
            "--port=9000",
        )
        val command = parseCommand(mockParams)
        val tasks = parseTasks(command.tasksFile ?: "")
        val users = parseUsers(command.usersFile ?: "")
        val jettyServer = app.asServer(Netty(9000)).start()
    }
    catch (e: Exception){
        println(e.message)
        exitProcess(1)
    }
}
