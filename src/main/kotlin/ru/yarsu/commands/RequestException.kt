package ru.yarsu.commands

import org.http4k.core.Status

class RequestException : Exception {
    val status: Status
    constructor() : super() {
        status = Status.BAD_REQUEST
    }

    constructor(message: String, status: Status = Status.BAD_REQUEST) : super(message) {
        this.status = status
    }
}
