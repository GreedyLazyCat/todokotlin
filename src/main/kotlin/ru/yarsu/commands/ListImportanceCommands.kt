package ru.yarsu.commands

import com.beust.jcommander.Parameter

class ListImportanceCommands: BaseCommand() {

    @Parameter(names = ["--important"])
    var important: String? = null

    @Parameter(names = ["--urgent"])
    var urgent: String? = null
}
