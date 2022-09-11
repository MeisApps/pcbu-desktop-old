package com.meisapps.pcbiounlock.server.packets

abstract class Packet {
    abstract fun getPacketId(): Int

    open fun read(b: ByteArray) {
    }

    open fun write() : ByteArray {
        return ByteArray(0)
    }
}