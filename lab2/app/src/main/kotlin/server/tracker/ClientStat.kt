package org.example.app.server.tracker

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ClientStat {
    val startTime = System.currentTimeMillis()

    var bytesDownloadedTotal = AtomicLong(0)

    var lastCheckBytes : Long = 0

    var hasPrinted = AtomicBoolean(false)

    var lastTimeCheck : Long = System.currentTimeMillis()
}