package com.meisapps.pcbiounlock.service

import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.service.linux.PamDeviceStorage
import com.meisapps.pcbiounlock.service.windows.WinDeviceStorage
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class PairedDevice(val pairingId: String, val pairingMethod: PairingMethod,
                        val deviceName: String, val userName: String, val encryptionKey: String,
                        val ipAddress: String, val bluetoothAddress: String, val cloudToken: String)

abstract class DeviceStorage {
    companion object {
        private var storageObj: DeviceStorage? = null

        fun getForPlatform(shell: Shell): DeviceStorage? {
            if(storageObj != null)
                return storageObj
            storageObj = if(OperatingSystem.isLinux)
                            PamDeviceStorage(shell)
                         else if(OperatingSystem.isWindows)
                            WinDeviceStorage()
                         else null
            return storageObj
        }
    }

    fun init() {
        if(!isConfigValid()) {
            clearData()
        }
    }

    private fun isConfigValid() : Boolean {
        val configFile = File(getConfigFilePath())
        return configFile.exists() && getDevices().isNotEmpty()
    }

    fun addDevice(device: PairedDevice) {
        val devices = getDevices().toMutableList()
        devices.removeIf { d -> d.pairingId == device.pairingId }
        devices.add(device)
        saveDevices(devices)
    }

    fun removeDevice(pairingId: String) {
        val devices = getDevices().toMutableList()
        devices.removeIf { d -> d.pairingId == pairingId }
        saveDevices(devices)
    }

    abstract fun clearData()
    abstract fun getConfigFilePath() : String

    abstract fun getDevices() : List<PairedDevice>
    abstract fun saveDevices(devices: List<PairedDevice>)
}
