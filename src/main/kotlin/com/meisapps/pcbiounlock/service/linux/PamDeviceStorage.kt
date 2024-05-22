package com.meisapps.pcbiounlock.service.linux

import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.PairedDevice
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.exceptions.ErrorMessageException
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PamDeviceStorage(private val shell: Shell) : DeviceStorage() {
    override fun clearData() {
        shell.runCommand("rm ${getConfigFilePath()}")
    }

    override fun getConfigFilePath() : String {
        return AppSettings.getBaseDir() + "paired_device.json"
    }

    override fun getDevices() : List<PairedDevice> {
        return try {
            val shell = Shell.getForPlatform()!!
            val dataStr = shell.readBytes(getConfigFilePath())!!.toString(Charsets.UTF_8)
            Json.decodeFromString<List<PairedDevice>>(dataStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveDevices(devices: List<PairedDevice>) {
        shell.writeBytes(getConfigFilePath(), Json.encodeToString(devices).toByteArray(Charsets.UTF_8))
        val protectResult = shell.runCommand("chown root:root ${getConfigFilePath()}").exitCode == 0 &&
                shell.runCommand("chmod 600 ${getConfigFilePath()}").exitCode == 0
        if(!protectResult)
            throw ErrorMessageException(I18n.get("data_protect_error"))
    }
}
