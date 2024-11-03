package ru.yarsu.commands

import com.beust.jcommander.Parameter

// @Parameters(separators = "=")
open class BaseCommand {
    @Parameter(names = ["--tasks-file"], required = true, arity = 1)
    var tasksFile: String? = null

    @Parameter(names = ["--users-file"], required = true, arity = 1)
    var usersFile: String? = null

    @Parameter(names = ["--port"], required = true, arity = 1)
    var port: Int? = null
}
