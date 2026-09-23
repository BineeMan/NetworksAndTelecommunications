package org.example.app.server

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

fun main(args: Array<String>) {
    val argParser = ArgParser("File server")
    val port by argParser.argument(type = ArgType.Int)
    argParser.parse(args)

    println("Server start!")
    val fileReceiver = FileReceiver(port, "uploads")
    fileReceiver.listenClients()
}