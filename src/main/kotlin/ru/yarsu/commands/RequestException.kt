package ru.yarsu.commands

class RequestException : Exception {
    constructor() : super()

    constructor(message: String) : super(message)
}
