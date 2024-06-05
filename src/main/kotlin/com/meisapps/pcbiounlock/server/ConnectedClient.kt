package com.meisapps.pcbiounlock.server

import com.meisapps.pcbiounlock.utils.AESUtils
import com.meisapps.pcbiounlock.utils.io.Console
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.Socket
import java.net.SocketException

class ConnectedClient(private val socket: Socket, private val encryptionKey: String, private val dataReceivedCallback: (client: ConnectedClient, b: ByteArray) -> Unit) {
    private val recvThread: Thread = Thread { receiveThread() }
    private var isRunning = true

    init {
        recvThread.start()
    }

    fun close(block: Boolean = true) {
        isRunning = false
        try {
            socket.close()
        } catch(_: Exception) {}
        if(block && recvThread.isAlive)
            recvThread.join()
    }

    fun sendPacket(packetStr: String) {
        val encData = AESUtils.encryptPacketData(packetStr.toByteArray(Charsets.UTF_8), encryptionKey)
        val stream = DataOutputStream(socket.getOutputStream())
        stream.writeShort(encData.size)
        stream.write(encData)
        stream.flush()
    }

    private fun receiveThread() {
        try {
            while (isRunning) {
                val stream = DataInputStream(socket.getInputStream())
                val length = stream.readUnsignedShort()
                val data = ByteArray(length)
                stream.readFully(data)
                dataReceivedCallback(this, data)
            }
        } catch (e: Exception) {
            if(e !is SocketException && e !is EOFException)
                Console.println(e.stackTraceToString())
        } finally {
            close(false)
        }
    }
}
