package com.meisapps.pcbiounlock.service

import com.meisapps.pcbiounlock.service.linux.LinuxServiceInstaller
import com.meisapps.pcbiounlock.service.windows.WindowsServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.storage.PCBUAppSettings
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.host.OperatingSystem

abstract class ServiceInstaller {
    companion object {
        fun getForPlatform(shell: Shell): ServiceInstaller? {
            if(OperatingSystem.isLinux)
                return LinuxServiceInstaller(shell)
            if(OperatingSystem.isWindows)
                return WindowsServiceInstaller(shell)

            return null
        }
    }

    fun reinstall() {
        if(isInstalled())
            uninstall(false)
        install()

        // Installed version
        val settings = AppSettings.get()
        AppSettings.set(PCBUAppSettings(VersionInfo.getAppVersion(), settings.language, settings.serverIP, settings.unlockServerPort, settings.pairingServerPort, settings.waitForKeyPress))
    }

    fun install() {
        if(isInstalled())
            return

        if(!isOpenSSLInstalled()) {
            installOpenSSL()
        }
        doInstall()

        // Installed version
        val settings = AppSettings.get()
        AppSettings.set(PCBUAppSettings(VersionInfo.getAppVersion(), settings.language, settings.serverIP, settings.unlockServerPort, settings.pairingServerPort, settings.waitForKeyPress))
    }

    fun uninstall(fullUninstall: Boolean = false) {
        if(!isInstalled())
            return

        doUninstall(fullUninstall)
        if(fullUninstall) {
            AppSettings.clear()
        } else {
            // Installed version
            val settings = AppSettings.get()
            AppSettings.set(PCBUAppSettings("", settings.language, settings.serverIP, settings.unlockServerPort, settings.pairingServerPort, settings.waitForKeyPress))
        }
    }

    abstract fun getModulePath() : String

    abstract fun isOpenSSLInstalled(): Boolean
    abstract fun isInstalled() : Boolean

    abstract fun installOpenSSL()

    protected abstract fun doInstall()
    protected abstract fun doUninstall(fullUninstall: Boolean)
}
