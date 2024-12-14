package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.lens.contentType
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.commands.BaseCommand
import ru.yarsu.data.factory.CategoryFactory
import ru.yarsu.data.factory.TasksFactory
import ru.yarsu.data.factory.UserFactory
import ru.yarsu.data.model.Category
import ru.yarsu.data.model.task.Task
import ru.yarsu.data.model.task.User
import ru.yarsu.data.storage.CategoryStorage
import ru.yarsu.data.storage.TaskStorage
import ru.yarsu.data.storage.UserStorage
import ru.yarsu.handlers.v1.ListEisenhowerHandler
import ru.yarsu.handlers.v1.ListTasksHandler
import ru.yarsu.handlers.v1.ListTimeHandler
import ru.yarsu.handlers.v1.StatisticHandler
import ru.yarsu.handlers.v1.TaskByIdHandler
import ru.yarsu.handlers.v2.CreateTaskHandler
import ru.yarsu.handlers.v2.DeleteUserHandler
import ru.yarsu.handlers.v2.ReadCategoriesHandler
import ru.yarsu.handlers.v2.ReadUsersHandler
import ru.yarsu.handlers.v2.UpdateCategoryHandler
import ru.yarsu.handlers.v2.UpdateTaskHandler
import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess

fun parseTasks(filePath: String): List<Task> {
    System.setProperty("file.encoding", "UTF8")
    var result: MutableList<Task> = mutableListOf()
    val file = File(filePath)

    csvReader().open(file) {
        readAllAsSequence().forEach { row: List<String> ->
            val task: Task? = TasksFactory().createTaskFromStringList(row)
            if (task != null) {
                result.add(task)
            }
        }
    }

    return result
}

fun parseUsers(filePath: String): List<User> {
    System.setProperty("file.encoding", "UTF8")
    var result: MutableList<User> = mutableListOf()
    val file: File = File(filePath)

    csvReader().open(file) {
        readAllAsSequence().forEach { row: List<String> ->
            val user: User? = UserFactory().createFromStringList(row)
            if (user != null) {
                result.add(user)
            }
        }
    }

    return result
}

fun parseCategories(filePath: String): List<Category> {
    System.setProperty("file.encoding", "UTF8")
    var result: MutableList<Category> = mutableListOf()
    val file: File = File(filePath)

    csvReader().open(file) {
        readAllAsSequence().forEach { row: List<String> ->
            val category: Category? = CategoryFactory().createFromStringList(row)
            if (category != null) {
                result.add(category)
            }
        }
    }

    return result
}

fun parseCommand(params: Array<String>): BaseCommand {
    val baseCommand = BaseCommand()

    val commander: JCommander = JCommander.newBuilder().addObject(baseCommand).build()

    commander.parse(*params)
    return baseCommand
}

fun createV1ApiRoutes(
    taskStorage: TaskStorage,
    userStorage: UserStorage,
    categoryStorage: CategoryStorage,
): RoutingHttpHandler {
    val listTasksHandler = ListTasksHandler(taskStorage)
    val taskByIdHandler = TaskByIdHandler(taskStorage, userStorage, categoryStorage)
    val listEisenhowerHandler = ListEisenhowerHandler(taskStorage)
    val listTimeHandler = ListTimeHandler(taskStorage)
    val statisticHandler = StatisticHandler(taskStorage)

    val taskRoutes =
        routes(
            "{task-id}" bind Method.GET to taskByIdHandler,
        )

    return routes(
        "list-tasks" bind Method.GET to listTasksHandler,
        "task/{task-id}" bind Method.GET to taskByIdHandler,
        "task" bind Method.GET to {
            Response(
                Status.BAD_REQUEST,
            ).contentType(ContentType.APPLICATION_JSON).body(generateErrorBody("Отсутствует обязательный параметр task-id"))
        },
        "list-eisenhower" bind Method.GET to listEisenhowerHandler,
        "list-time" bind Method.GET to listTimeHandler,
        "statistic" bind Method.GET to statisticHandler,
    )
}

fun createV2ApiRoutes(
    taskStorage: TaskStorage,
    userStorage: UserStorage,
    categoryStorage: CategoryStorage,
): RoutingHttpHandler {
    val listTasksHandler = ListTasksHandler(taskStorage)
    val taskByIdHandler = TaskByIdHandler(taskStorage, userStorage, categoryStorage)
    val listEisenhowerHandler = ListEisenhowerHandler(taskStorage)
    val listTimeHandler = ListTimeHandler(taskStorage)
    val statisticHandler = StatisticHandler(taskStorage)

    val taskCreateHandler = CreateTaskHandler(taskStorage, userStorage, categoryStorage)
    val taskUpdateHandler = UpdateTaskHandler(categoryStorage, userStorage, taskStorage)

    val readCategoriesHandler = ReadCategoriesHandler(categoryStorage, userStorage)
    val updateCategoryHandler = UpdateCategoryHandler(categoryStorage, userStorage)

    val readUsersHandler = ReadUsersHandler(userStorage)
    val deleteUserHandler = DeleteUserHandler(userStorage, categoryStorage, taskStorage)

    return jsonContentTypeFilter.then(requestExceptionFilter()).then(lensFailureFilter()).then(
        routes(
            "tasks" bind Method.GET to listTasksHandler,
            "tasks" bind Method.POST to taskCreateHandler,
            "tasks/{task-id}" bind Method.GET to taskByIdHandler,
            "tasks/{task-id}" bind Method.PUT to taskUpdateHandler,
            "tasks/eisenhower" bind Method.GET to listEisenhowerHandler,
            "tasks/by-time" bind Method.GET to listTimeHandler,
            "tasks/statistics" bind Method.GET to statisticHandler,
            "categories" bind Method.GET to readCategoriesHandler,
            "categories/{category-id}" bind Method.PUT to updateCategoryHandler,
            "users" bind Method.GET to readUsersHandler,
            "users/{user-id}" bind Method.DELETE to deleteUserHandler,
        ),
    )
}

fun main(params: Array<String>) {
    try {
//        val mockParams =
//            arrayOf(
//                "--tasks-file=src/main/resources/tasks.csv",
//                "--users-file=src/main/resources/users.csv",
//                "--port=9000",
//            )
        val command = parseCommand(params)
        val tasks = parseTasks(command.tasksFile ?: "")
        val users = parseUsers(command.usersFile ?: "")
        val categories = parseCategories(command.categoriesFile ?: "")

        val taskStorage = TaskStorage(tasks)
        val userStorage = UserStorage(users)
        val categoryStorage = CategoryStorage(categories)

        val apiRoutes = createV1ApiRoutes(taskStorage, userStorage, categoryStorage)
        val apiRoutesV2 = createV2ApiRoutes(taskStorage, userStorage, categoryStorage)

        val app: HttpHandler =
            routes(
                "ping" bind Method.GET to {
                    Response(Status.OK).body("Приложение запущено")
                },
                "v1" bind apiRoutes,
                "v2" bind apiRoutesV2,
            )

        app.asServer(Netty(command.port ?: 9000)).start()
    } catch (e: ParameterException) {
        println(e.message)
        exitProcess(1)
    } catch (e: FileNotFoundException) {
        println(e.message)
        exitProcess(1)
    }
}
