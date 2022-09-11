package com.meisapps.pcbiounlock.server

import com.meisapps.pcbiounlock.server.packets.Packet
import com.meisapps.pcbiounlock.utils.AESUtils
import com.meisapps.pcbiounlock.utils.io.Console
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.net.SocketException


class ConnectedClient(private val socket: Socket, private val encryptionKey: String, private val dataReceivedCallback: (client: ConnectedClient, b: ByteArray) -> Unit) {
    private val recvThread: Thread = Thread { receiveThread(socket) }
    private var isRunning = true

    init {
        recvThread.start()
    }

    fun close() {
        isRunning = false
        socket.close()
        recvThread.join()
    }

    fun sendPacket(packet: Packet) {
        val packetStream = ByteArrayOutputStream()
        packetStream.write(packet.getPacketId().toUByte().toInt())
        packetStream.write(AESUtils.encryptPacketData(packet.write(), encryptionKey))

        val outputStream = socket.getOutputStream()
        outputStream.write(packetStream.toByteArray())
        outputStream.flush()
    }

    private fun receiveThread(client: Socket) {
        val inputStream = client.getInputStream()
        val buffer = ByteArray(1024)

        try {
            while (isRunning) {
                val i = inputStream.read(buffer, 0, buffer.size)
                if(i == -1)
                    break

                dataReceivedCallback(this, buffer.copyOf(i))
            }
        } catch (e: Exception) {
            if(e !is SocketException)
                Console.println(e.stackTraceToString())
        } finally {
            client.close()
        }
    }
}