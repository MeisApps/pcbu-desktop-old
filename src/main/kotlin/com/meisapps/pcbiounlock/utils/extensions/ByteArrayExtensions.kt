package com.meisapps.pcbiounlock.utils.extensions

import java.nio.ByteBuffer
import java.nio.charset.Charset


fun ByteArray.toKString(charset: Charset): String {
    val strippedList = ArrayList<Byte>(size)
    for(byte in this) {
        if(byte.toInt() == 0x00)
            break
        strippedList.add(byte)
    }

    val strippedBytes = strippedList.toByteArray()
    if(strippedBytes.isEmpty())
        return ""

    val charBuf = charset.decode(ByteBuffer.wrap(strippedBytes))
    return charBuf.toString()
}