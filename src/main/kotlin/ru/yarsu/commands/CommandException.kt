package ru.yarsu.commands

class CommandException : Exception {
    constructor() : super()

    constructor(message: String) : super(message)
}
