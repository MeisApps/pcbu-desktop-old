package com.meisapps.pcbiounlock

import com.meisapps.pcbiounlock.natives.LinuxUtils
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostArchitecture
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.text.I18n
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane
import kotlin.system.exitProcess


object StartupHelper {
    fun verifyEnvironment() {
        // Check platform
        val shell = Shell.getForPlatform() ?: throw ErrorMessageException(I18n.get("unsupported_os"))
        ServiceInstaller.getForPlatform(shell) ?: throw ErrorMessageException(I18n.get("unsupported_os"))
        DeviceStorage.getForPlatform(shell) ?: throw ErrorMessageException(I18n.get("unsupported_os"))

        // Licences
        if(ResourceHelper.getFileBytes("licenses.txt") == null)
            throw ErrorMessageException(I18n.get("error_resources_corrupt"))

        // Linux
        if(OperatingSystem.isLinux) {
            // Is supported arch
            if(HostUtils.getCpuArchitecture() == HostArchitecture.UNKNOWN)
                throw ErrorMessageException(I18n.get("unsupported_arch"))

            // Has shadow
            if(shell.runUserCommand("test -f /etc/shadow").exitCode != 0)
                throw ErrorMessageException(I18n.get("error_linux_shadow_file"))

            // Has systemd
            if(shell.runUserCommand("which systemctl").exitCode != 0)
                throw ErrorMessageException(I18n.get("error_linux_systemd_required"))

            // Has bash
            if(shell.runUserCommand("which bash").exitCode != 0)
                throw ErrorMessageException(I18n.get("error_linux_required_dep", "Bash"))

            // Has libcrypt
            if(!LinuxUtils.hasSharedLibrary("libcrypt.so.1"))
                throw ErrorMessageException(I18n.get("error_linux_required_dep", "libcrypt"))

            ResourceHelper.getNativeByName(ResourceHelper.PcbuAuthFileName)
            ResourceHelper.getNativeByName(ResourceHelper.PamModuleFileName)
            ResourceHelper.getNativeByName(ResourceHelper.LinuxCryptoFileName)
            ResourceHelper.getNativeByName(ResourceHelper.LinuxSSLFileName)
        }

        // Windows
        if(OperatingSystem.isWindows) {
            // Is 64 bit
            if(HostUtils.getCpuArchitecture() != HostArchitecture.X86_64 && HostUtils.getCpuArchitecture() != HostArchitecture.ARM64)
                throw ErrorMessageException(I18n.get("unsupported_arch"))
            val isArm = HostUtils.getCpuArchitecture() == HostArchitecture.ARM64

            // Has VC Redist
            var hasRedist = false
            try {
                val redistBuild = Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\VisualStudio\\14.0\\VC\\Runtimes\\" + if(isArm) "arm64" else "x64",
                    "Bld"
                )
                hasRedist = redistBuild >= 0x7a5e
            } catch (_: Exception) { }

            if(!hasRedist) {
                val dialogResult = JOptionPane.showConfirmDialog(
                    null,
                    I18n.get("win_redist_error"),
                    I18n.get("error"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
                )
                if (dialogResult == JOptionPane.YES_OPTION) {
                    val link = "https://aka.ms/vs/17/release/vc_redist." + (if(isArm) "arm64" else "x64") + ".exe"
                    Desktop.getDesktop().browse(URI(link))
                }
                exitProcess(1)
            }

            ResourceHelper.getNativeByName(ResourceHelper.WinModuleFileName)
            if(isArm) {
                ResourceHelper.getNativeByName(ResourceHelper.WinARM64CryptoFileName)
                ResourceHelper.getNativeByName(ResourceHelper.WinARM64SSLFileName)
            } else {
                ResourceHelper.getNativeByName(ResourceHelper.WinX64CryptoFileName)
                ResourceHelper.getNativeByName(ResourceHelper.WinX64SSLFileName)
            }
        }

        Console.println("Resources verified.")
    }
}