package com.meisapps.pcbiounlock.utils.text

import java.math.BigInteger
import java.security.SecureRandom


object StringUtils {
    private val secureRandom = SecureRandom()

    fun generateRandomHexToken(byteLength: Int): String {
        val token = ByteArray(byteLength)
        secureRandom.nextBytes(token)
        return BigInteger(1, token).toString(16)
    }

    fun generateRandomString(len: Int): String {
        val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@#+-*/=$%&!?.:,;-_<>|"
        val sb = StringBuilder(len)
        for (i in 0 until len)
            sb.append(AB[secureRandom.nextInt(AB.length)])
        return sb.toString()
    }
}