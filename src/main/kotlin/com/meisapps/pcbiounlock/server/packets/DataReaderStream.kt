package com.meisapps.pcbiounlock.server.packets

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class DataReaderStream(b: ByteArray) : DataInputStream(ByteArrayInputStream(b)) {
    fun readString() : String {
        val len = readInt()
        var str = ""
        for(i in 0 until len) {
            str += readChar()
        }

        return str
    }
}

class DataWriterStream : DataOutputStream(ByteArrayOutputStream()) {
    fun writeString(str: String) {
        writeInt(str.length)
        for(c in str) {
            writeChar(c.code)
        }
    }

    fun toByteArray() : ByteArray {
        val byteStream = out as ByteArrayOutputStream
        return byteStream.toByteArray()
    }
}