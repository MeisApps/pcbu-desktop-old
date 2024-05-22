package com.meisapps.pcbiounlock.service.windows

import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.PairedDevice
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.exceptions.ErrorMessageException
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class WinDeviceStorage : DeviceStorage() {
    override fun clearData() {
        try {
            protectFile(false)
        } catch (_: Exception) {}
        Files.deleteIfExists(Paths.get(getConfigFilePath()))
    }

    override fun getConfigFilePath() : String {
        return AppSettings.getBaseDir() + "paired_device.json"
    }

    override fun getDevices() : List<PairedDevice> {
        return try {
            protectFile(false)
            val shell = Shell.getForPlatform()!!
            val dataStr = shell.readBytes(getConfigFilePath())!!.toString(Charsets.UTF_8)
            protectFile(true)
            Json.decodeFromString<List<PairedDevice>>(dataStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveDevices(devices: List<PairedDevice>) {
        if(File(getConfigFilePath()).exists())
            protectFile(false)
        Files.write(Paths.get(getConfigFilePath()), Json.encodeToString(devices).toByteArray(Charsets.UTF_8))
        protectFile(true)
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
}
