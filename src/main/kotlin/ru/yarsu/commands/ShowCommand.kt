package ru.yarsu.commands

import com.beust.jcommander.Parameter


class ShowCommand : BaseCommand() {

    @Parameter(names = ["--task-id"], required = true, arity = 1)
    var taskId: String? = null
}
