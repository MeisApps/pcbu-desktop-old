package com.meisapps.pcbiounlock.service.linux

import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.PairedDevice
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.natives.LinuxUtils
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.json.Json
import java.io.File


class PamDeviceStorage(private val shell: Shell) : DeviceStorage() {
    override fun savePairData(pairingId: String, deviceName: String, messagingToken: String, ipAddress: String, bluetoothAddress: String, encryptionKey: String) {
        val pairedDevice = PairedDevice(pairingId, deviceName, messagingToken, ipAddress, bluetoothAddress, encryptionKey, LinuxUtils.getCurrentUserName())
        shell.writeBytes(getConfigFilePath(), Json.encodeToString(PairedDevice.serializer(), pairedDevice).toByteArray(Charsets.UTF_8))

        val protectResult = shell.runCommand("chown root:root ${getConfigFilePath()}").exitCode == 0 &&
                            shell.runCommand("chmod 600 ${getConfigFilePath()}").exitCode == 0
        if(!protectResult)
            throw ErrorMessageException(I18n.get("data_protect_error"))
    }

    override fun clearData() {
        shell.runCommand("rm ${getConfigFilePath()}")
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
            val shell = Shell.getForPlatform()!!
            val dataStr = shell.readBytes(getConfigFilePath())!!.toString(Charsets.UTF_8)
            Json.decodeFromString(PairedDevice.serializer(), dataStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun getConfigFilePath() : String {
        return AppSettings.getBaseDir() + "paired_device.json"
    }
}