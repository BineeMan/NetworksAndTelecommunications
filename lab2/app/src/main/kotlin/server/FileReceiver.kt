package org.example.app.server

import org.example.app.common.FILE_LENGTH_LIMIT
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.math.min

class FileReceiver(val port: Int,
                   val saveDirectory: String) {

    private val uploadsDir : Path = Path.of(saveDirectory).toAbsolutePath().normalize()

    val bufferSize = 64 * 1024

    init {
        Files.createDirectories(uploadsDir);
    }

    fun listenClients() {
        val threadPool = Executors.newCachedThreadPool()
        try {
            ServerSocket(port).use { serverSocket ->
                while (!Thread.currentThread().isInterrupted) {
                    val socket = serverSocket.accept()
                    val clientAddress = "${socket.inetAddress.hostAddress}:${socket.port}"
                    println("Новое подключение: $clientAddress")

                    threadPool.execute {
                        try {
                            handleClient(socket)
                        } catch (e: Exception) {
                            println("Ошибка при работе с клиентом: $clientAddress")
                        } finally {
                            println("Соединение с клиентом $clientAddress закрыто")
                        }
                    }

                }
            }
        } catch (e: Exception) {
            println("Критическая ошибка сервера: ${e.message}")
        } finally {
            threadPool.shutdown()
        }
    }

    fun handleClient(socket: Socket) {
        socket.use { socket ->
            val inStream = DataInputStream(socket.getInputStream())
            val outStream = DataOutputStream(socket.getOutputStream())

            val fileNameBytesLength = inStream.readInt()
            if (fileNameBytesLength !in 1..FILE_LENGTH_LIMIT) {
                println("fileNameBytesSize !in 1..FILE_LENGTH_LIMIT")
                outStream.writeBoolean(false)
                return
            }

            val fileNameBytes = inStream.readNBytes(fileNameBytesLength);
            val fileName = String(fileNameBytes, Charsets.UTF_8)

            val cleanFileName = Path.of(fileName).fileName?.toString() ?: "unnamed_file"
            var targetPath = uploadsDir.resolve(cleanFileName).normalize()
            if (targetPath.exists()) {
                targetPath = getNextFilePath(targetPath)
            }

            val expectedFileSize = inStream.readLong()

            var fileBytesReceived : Long = 0
            val buffer = ByteArray(bufferSize)

            try {
                FileOutputStream(targetPath.toFile()).use { fileOut->
                    var remaining = expectedFileSize
                    while (remaining > 0) {
                        val bytesToRead = min(buffer.size.toLong(), remaining).toInt()
                        val bytesRead = inStream.read(buffer, 0, bytesToRead)

                        if (bytesRead == -1) {
                            break
                        }

                        fileOut.write(buffer, 0, bytesRead);
                        fileBytesReceived += bytesRead
                        remaining -= bytesRead
                    }
                }
            }
            catch (e: Exception) {
                Files.deleteIfExists(targetPath)
                outStream.writeBoolean(false)
                return
            }

            val isSuccess = (fileBytesReceived == expectedFileSize)
            outStream.writeBoolean(isSuccess)
            outStream.flush()

            if (!isSuccess) {
                Files.deleteIfExists(targetPath)
            }
        }
    }

    private fun getNextFilePath(path: Path): Path {
        if (!path.exists()) {
            return path
        }
        val parentDir = path.parent ?: Path.of("")
        val baseName = path.nameWithoutExtension // Например: "MyFile" вместо "MyFile.txt"
        val extension = path.extension           // Например: "txt"
        val dotExtension = if (extension.isNotEmpty()) ".$extension" else ""

        var counter = 1
        var newFilePath = path

        while (newFilePath.exists()) {
            val newFileName = "$baseName($counter)$dotExtension"
            newFilePath = parentDir.resolve(newFileName)
            counter++
        }

        return newFilePath
    }
}