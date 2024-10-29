package ru.yarsu.commands

import com.beust.jcommander.Parameter

class ListTimeCommand: BaseCommand() {

    @Parameter(names = ["--time"], required = true)
    var time: String? = null
}
