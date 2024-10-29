package ru.yarsu.commands

import com.beust.jcommander.Parameters
import com.beust.jcommander.Parameter

@Parameters(separators = "=")
open class BaseCommand {
    @Parameter(names = ["--tasks-file"], arity = 1)
    var tasksFile: String? = null

    @Parameter(names = ["--users-file"], arity = 1)
    var usersFile: String? = null

    @Parameter(names = ["--port"], arity = 1)
    var port: Int? = null
}
