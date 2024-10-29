package ru.yarsu.commands

import com.beust.jcommander.Parameter

class StatsCommand: BaseCommand() {

    @Parameter(names = ["--by-date"])
    var byDate: String? = null
}
