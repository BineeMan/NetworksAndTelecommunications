package org.example.app.client
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.nio.file.Paths
import kotlin.io.path.exists

fun main(args: Array<String>) {
    val parser = ArgParser("client");
    var filePathString by parser.argument(type = ArgType.String)
    var host by parser.argument(type = ArgType.String)
    var port by parser.argument(type = ArgType.Int)
    parser.parse(args);

    val filePath = Paths.get(filePathString).toAbsolutePath()
    if (!filePath.exists()) {
        println("Файл $filePath не найден!")
        return
    }

    println("Отправляем файл через $filePathString")
    val fileSender = FileSender(host, port)
    val result = fileSender.sendFile(filePath.toFile())
    println(if (result) "Успешно передали файл" else "Файл не удалось передать")
}
