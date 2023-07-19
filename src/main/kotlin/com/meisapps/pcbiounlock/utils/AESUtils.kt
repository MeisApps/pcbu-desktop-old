package com.meisapps.pcbiounlock.utils

import com.meisapps.pcbiounlock.utils.io.Console
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object AESUtils {
    fun sha1(base: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hash = digest.digest(base.toByteArray(charset("UTF-8")))
        val hexString = StringBuilder()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private const val PACKET_TIMEOUT = 60000 * 2

    fun encryptPacketData(data: ByteArray, key: String): ByteArray {
        val secureRandom = SecureRandom()
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val secretKey: SecretKey = generateSecretKey(key, iv)
        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        // Encrypted Data
        val dataBuffer = ByteBuffer.allocate(8 + data.size)
        dataBuffer.putLong(System.currentTimeMillis())
        dataBuffer.put(data)

        //Encrypt the data
        val encryptedData: ByteArray = cipher.doFinal(dataBuffer.array())

        //Concatenate everything and return the final data
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(4 + iv.size + encryptedData.size)
        byteBuffer.putInt(iv.size)
        byteBuffer.put(iv)
        byteBuffer.put(encryptedData)
        return byteBuffer.array()
    }

    fun decryptPacketData(encryptedData: ByteArray, key: String): ByteArray {
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(encryptedData)
        val nonceSize: Int = byteBuffer.int
        if(nonceSize < 12 || nonceSize >= 16)
            throw Exception("Invalid nonce size on AES data !")

        val iv = ByteArray(nonceSize)
        byteBuffer.get(iv)
        val secretKey: SecretKey = generateSecretKey(key, iv)

        val cipherBytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(cipherBytes)
        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        // Decrypt the data
        val decData = cipher.doFinal(cipherBytes)
        val decBuffer = ByteBuffer.wrap(decData)

        val timestamp = decBuffer.long
        val timeDiff = System.currentTimeMillis() - timestamp
        if(timeDiff < -PACKET_TIMEOUT || timeDiff > PACKET_TIMEOUT) {
            Console.println("Time diff: $timeDiff")
            throw Exception("Invalid timestamp on AES data !")
        }

        val resArr = ByteArray(decBuffer.remaining())
        decBuffer.get(resArr)
        return resArr
    }

    private fun generateSecretKey(password: String, iv: ByteArray?) : SecretKey {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), iv, 65536, 128) // AES-128
        val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val key: ByteArray = secretKeyFactory.generateSecret(spec).encoded
        return SecretKeySpec(key, "AES")
    }
}