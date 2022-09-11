package com.meisapps.pcbiounlock.service.windows

import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.PairedDevice
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.natives.WinUtils
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class WinDeviceStorage : DeviceStorage() {
    override fun savePairData(pairingId: String, deviceName: String, messagingToken: String, ipAddress: String, bluetoothAddress: String, encryptionKey: String) {
        val pairedDevice = PairedDevice(pairingId, deviceName, messagingToken, ipAddress, bluetoothAddress, encryptionKey, WinUtils.getCurrentUserName())
        if(File(getConfigFilePath()).exists())
            protectFile(false)

        Files.write(Paths.get(getConfigFilePath()), Json.encodeToString(PairedDevice.serializer(), pairedDevice).toByteArray(Charsets.UTF_8))
        protectFile(true)
    }

    override fun clearData() {
        try {
            protectFile(false)
        } catch (_: Exception) {}
        Files.deleteIfExists(Paths.get(getConfigFilePath()))
    }

    override fun isConfigValid(): Boolean {
        val configFile = File(getConfigFilePath())
        return configFile.exists() && getConfig() != null
    }

    override fun isPaired(): Boolean {
        return isConfigValid()
    }

    override fun getDeviceName(): String {
        if(!isPaired())
            return "None"
        return getConfig()!!.deviceName
    }

    private fun getConfig(): PairedDevice? {
        return try {
            protectFile(false)
            val shell = Shell.getForPlatform()!!
            val dataStr = shell.readBytes(getConfigFilePath())!!.toString(Charsets.UTF_8)
            protectFile(true)
            Json.decodeFromString(PairedDevice.serializer(), dataStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun protectFile(enabled: Boolean) {
        val shell = Shell.getForPlatform()!!
        val filePath = getConfigFilePath().replace('/', '\\')
        if(enabled) {
            if(shell.runCommand("icacls $filePath /deny *S-1-5-32-545:F").exitCode != 0)
                throw ErrorMessageException(I18n.get("data_protect_error"))
        } else {
            if(shell.runCommand("icacls $filePath /grant *S-1-5-32-545:F").exitCode != 0)
                throw ErrorMessageException(I18n.get("data_protect_error"))
        }
    }

    private fun getConfigFilePath() : String {
        return AppSettings.getBaseDir() + "paired_device.json"
    }
}