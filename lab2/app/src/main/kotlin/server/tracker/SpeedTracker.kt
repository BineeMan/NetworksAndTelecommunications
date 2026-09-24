    package org.example.app.server.tracker

    import java.util.concurrent.ConcurrentHashMap
    import kotlin.concurrent.thread

    class SpeedTracker(val refreshRateMs : Long) {
        val activeClients = ConcurrentHashMap<String, ClientStat>()

        var thread: Thread? = null

        fun startTracking() {
            thread = thread(start = true) {
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        Thread.sleep(refreshRateMs)
                        val now = System.currentTimeMillis()
                        for ((id, client) in activeClients) {
                            printSpeedForClient(id, client, now)
                            client.hasPrinted.set(true)
                            client.lastCheckBytes = client.bytesDownloadedTotal.get()
                            client.lastTimeCheck = now
                        }

                    }
                } catch (e: InterruptedException) {
                }
            }
        }

        fun stopTracking() {
            thread?.interrupt()
            thread?.join()
        }

        fun addBytes(clientId: String, bytesCount: Long) {
            val stat = activeClients[clientId]
            if (stat == null) {
                println("Failed to find client with id: $clientId")
                return
            }
            stat.bytesDownloadedTotal.addAndGet(bytesCount)

        }

        fun addClient(clientId: String) {
            if (activeClients.containsKey(clientId)) {
                println("Failed to add client with id, already exist: $clientId")
                return
            }
            activeClients[clientId] = ClientStat()
        }

        fun removeClient(clientId: String) {
            val stat = activeClients.remove(clientId)
            if (stat == null) {
                println("Не удалось удалить клиента $clientId: не найден")
                return
            }
            if (!stat.hasPrinted.compareAndSet(false, true)) {
                printSpeedForClient(clientId, stat, System.currentTimeMillis())
            }
        }

        private fun printSpeedForClient(clientId: String, clientStat: ClientStat, now: Long) {
            val bytesDelta = (clientStat.bytesDownloadedTotal.get() - clientStat.lastCheckBytes).toDouble()
            val timeDelta
            val deltaSpeed : Double = bytesDelta /
            val averageSpeed : Double =
                clientStat.bytesDownloadedTotal.get().toDouble()  / ((now - clientStat.startTime) / 1000.0)

            println("[$clientId] Мгновенная: ${formatSpeed(deltaSpeed)} | Средняя: ${formatSpeed(averageSpeed)}")
        }

        private fun formatSpeed(bytesPerSec: Double): String {
            return when {
                bytesPerSec >= 1024 * 1024 -> String.format("%.2f МБ/с", bytesPerSec / (1024 * 1024))
                bytesPerSec >= 1024 -> String.format("%.2f КБ/с", bytesPerSec / 1024)
                else -> String.format("%.2f Б/с", bytesPerSec)
            }
        }
    }