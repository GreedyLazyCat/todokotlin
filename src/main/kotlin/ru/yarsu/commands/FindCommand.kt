package ru.yarsu.commands

import com.beust.jcommander.Parameter

class FindCommand : BaseCommand() {
    @Parameter(names = ["--text"], required = true)
    var text: String? = null
}
