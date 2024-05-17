package com.meisapps.pcbiounlock.utils

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.digest.SHA3
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private const val PACKET_TIMEOUT = 60000 * 2

    private const val AES_KEY_SIZE = 256
    private const val IV_SIZE = 16
    private const val SALT_SIZE = 16
    private const val ITERATIONS = 65535

    fun sha256(base: String): String {
        val digest = SHA3.Digest256()
        val hash = digest.digest(base.toByteArray(charset("UTF-8")))
        val hexString = StringBuilder()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    fun encryptPacketData(data: ByteArray, key: String): ByteArray {
        val dataBuffer = ByteBuffer.allocate(8 + data.size)
        dataBuffer.putLong(System.currentTimeMillis())
        dataBuffer.put(data)
        return encryptAES(dataBuffer.array(), key)
    }

    fun decryptPacketData(encryptedData: ByteArray, key: String): ByteArray {
        val decData = decryptAES(encryptedData, key)
        val decBuffer = ByteBuffer.wrap(decData)

        val timestamp = decBuffer.long
        val timeDiff = System.currentTimeMillis() - timestamp
        if(timeDiff < -PACKET_TIMEOUT || timeDiff > PACKET_TIMEOUT)
            throw Exception("Invalid timestamp on AES data!")

        val resArr = ByteArray(decBuffer.remaining())
        decBuffer.get(resArr)
        return resArr
    }

    private fun encryptAES(src: ByteArray?, password: String): ByteArray {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        val key = generateKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(src)

        val result = ByteArray(IV_SIZE + SALT_SIZE + encrypted.size)
        System.arraycopy(iv, 0, result, 0, IV_SIZE)
        System.arraycopy(salt, 0, result, IV_SIZE, SALT_SIZE)
        System.arraycopy(encrypted, 0, result, IV_SIZE + SALT_SIZE, encrypted.size)
        return result
    }

    private fun decryptAES(src: ByteArray, password: String): ByteArray {
        val iv = Arrays.copyOfRange(src, 0, IV_SIZE)
        val salt = Arrays.copyOfRange(src, IV_SIZE, IV_SIZE + SALT_SIZE)
        val encrypted = Arrays.copyOfRange(src, IV_SIZE + SALT_SIZE, src.size)
        val key = generateKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(encrypted)
    }

    private fun generateKey(password: String, salt: ByteArray): ByteArray {
        val generator = PKCS5S2ParametersGenerator(SHA256Digest())
        generator.init(password.toByteArray(), salt, ITERATIONS)
        return (generator.generateDerivedParameters(AES_KEY_SIZE) as KeyParameter).key
    }
}
