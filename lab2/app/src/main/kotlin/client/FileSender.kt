package org.example.app.client
import org.example.app.common.FILE_LENGTH_LIMIT
import org.example.app.common.ONE_TERABYTE_BYTES
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.Socket
import java.nio.charset.Charset

class FileSender(val host: String, val port: Int) {

    val bufferSize = 16 * 1042

    public fun sendFile(file: File) : Boolean {
        require(file.exists()) { "Указанный файл не существует: ${file.path}" };

        val fileName = file.name;
        val fileNameBytes = file.name.toByteArray(Charsets.UTF_8);
        require(fileNameBytes.size < FILE_LENGTH_LIMIT) { "Имя файла превышает 4096 байт" };

        val fileSize = file.length()
        require(fileSize < ONE_TERABYTE_BYTES)  { "Размер файла превышает 1 ТБ" };


        Socket(host, port).use { socket ->
            val out = DataOutputStream(socket.getOutputStream())
            val inStream = DataInputStream(socket.getInputStream())

            out.writeInt(fileNameBytes.size)
            out.write(fileNameBytes)
            out.writeLong(fileSize)

            println("Отправка файла: $fileName (${fileSize} байт)...")
            FileInputStream(file).use { fileIn ->
                val buffer = ByteArray(bufferSize);
                var bytesRead = 0;
                while (fileIn.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                    Thread.sleep(1)
                }
            }

            out.flush()

            val isSuccess = inStream.readBoolean();
            return isSuccess;
        }
    }
}