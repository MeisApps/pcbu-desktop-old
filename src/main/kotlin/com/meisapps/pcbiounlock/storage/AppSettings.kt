package com.meisapps.pcbiounlock.storage

import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PCBUAppSettings(val installedVersion: String, val language: String, val serverIP: String, val unlockServerPort: Int, val pairingServerPort: Int)

object AppSettings {
    fun init() {
        val shell = Shell.getForPlatform()!!
        when(OperatingSystem.get()) {
            HostOS.LINUX -> shell.runCommand("mkdir ${getBaseDir()}")
            HostOS.WINDOWS -> shell.runCommand("mkdir ${getBaseDir().replace('/', '\\')}")
            else -> throw ErrorMessageException(I18n.get("unsupported_os"))
        }

        if(!File(getBaseDir()).exists())
            throw ErrorMessageException(I18n.get("base_dir_create_error"))
    }

    fun clear() {
        val shell = Shell.getForPlatform()!!
        when(OperatingSystem.get()) {
            HostOS.LINUX -> shell.runCommand("rm -R ${getBaseDir()}")
            HostOS.WINDOWS -> shell.runCommand("rd /s /q ${getBaseDir().replace('/', '\\')}")
            else -> throw ErrorMessageException(I18n.get("unsupported_os"))
        }

        if(File(getBaseDir()).exists())
            throw ErrorMessageException("Could not remove base directory !")
    }

    fun get(): PCBUAppSettings {
        val filePath = getBaseDir() + "app_settings.json"
        return try {
            val shell = Shell.getForPlatform()!!
            val dataStr = shell.readBytes(filePath)!!.toString(Charsets.UTF_8)
            Json.decodeFromString(PCBUAppSettings.serializer(), dataStr)
        } catch (e: Exception) {
            PCBUAppSettings("", "auto", "auto", 43296, 43295)
        }
    }

    fun set(settings: PCBUAppSettings) {
        init()
        val shell = Shell.getForPlatform()!!
        val dataStr = Json.encodeToString(PCBUAppSettings.serializer(), settings)
        shell.writeBytes(getBaseDir() + "app_settings.json", dataStr.toByteArray(Charsets.UTF_8))
    }

    fun getBaseDir(): String {
        return when(OperatingSystem.get()) {
            HostOS.LINUX -> "/etc/pc-bio-unlock/"
            HostOS.WINDOWS -> "C:/ProgramData/PCBioUnlock/"
            else -> throw ErrorMessageException(I18n.get("unsupported_os"))
        }
    }
}
