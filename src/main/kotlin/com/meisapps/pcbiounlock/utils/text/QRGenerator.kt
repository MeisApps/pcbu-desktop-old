package com.meisapps.pcbiounlock.utils.text

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.meisapps.pcbiounlock.utils.io.Console
import java.io.ByteArrayOutputStream


object QRGenerator {
    fun printQRCode(text: String, width: Int, height: Int) {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)

        val repeatPerModule = 1
        val darkColorString = "██"
        val whiteSpaceString = "  "
        val drawQuietZones = true

        val qrCode = ArrayList<String>()
        val quietZonesModifier = if (drawQuietZones) 0 else 8
        val quietZonesOffset = (quietZonesModifier * 0.5).toInt()

        val adjustmentValueForNumberOfCharacters = 0
        val verticalNumberOfRepeats = repeatPerModule + adjustmentValueForNumberOfCharacters

        val sideLength = (bitMatrix.width - quietZonesModifier) * verticalNumberOfRepeats

        for (y in 0 until sideLength) {
            val lineBuilder = StringBuilder()
            for (x in 0 until bitMatrix.width - quietZonesModifier) {
                val module = bitMatrix.get(x + quietZonesOffset, (y + verticalNumberOfRepeats) / verticalNumberOfRepeats - 1 + quietZonesOffset)
                for (i in 0 until repeatPerModule) {
                    lineBuilder.append(if (module) darkColorString else whiteSpaceString)
                }
            }
            qrCode.add(lineBuilder.toString())
        }

        for(code in qrCode)
            Console.println(code)
    }

    fun getQRCodeImage(text: String, width: Int, height: Int) : ByteArray {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        val pngOutputStream = ByteArrayOutputStream()

        val con = MatrixToImageConfig(MatrixToImageConfig.WHITE, MatrixToImageConfig.BLACK)
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, con)
        return pngOutputStream.toByteArray()
    }
}