package com.meisapps.pcbiounlock.utils.io

import com.meisapps.pcbiounlock.utils.exceptions.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostArchitecture
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.Image
import javax.swing.ImageIcon


object ResourceHelper {
    private const val AppIconFileName = "icon.png"

    const val PcbuAuthFileName = "pcbu_auth"
    const val PamModuleFileName = "pam_pcbiounlock.so"
    const val WinModuleFileName = "win-pcbiounlock.dll"

    const val LinuxSSLFileName = "libssl.so.3"
    const val LinuxCryptoFileName = "libcrypto.so.3"
    const val SELinuxPolicyFileName = "pcbu_auth_policy.pp"

    const val WinX64SSLFileName = "libssl-3-x64.dll"
    const val WinX64CryptoFileName = "libcrypto-3-x64.dll"
    const val WinARM64SSLFileName = "libssl-3-arm64.dll"
    const val WinARM64CryptoFileName = "libcrypto-3-arm64.dll"

    fun getNativeModule() : ByteArray {
        return when(OperatingSystem.get()) {
            HostOS.WINDOWS -> getNativeByName(WinModuleFileName)
            HostOS.LINUX -> getNativeByName(PamModuleFileName)
            else -> throw ErrorMessageException(I18n.get("unsupported_os"))
        }
    }

    fun getNativeByName(fileName: String) : ByteArray {
        val archStr = when(HostUtils.getCpuArchitecture()) {
            HostArchitecture.X86 -> "x86"
            HostArchitecture.X86_64 -> "x64"
            HostArchitecture.ARM -> "arm"
            HostArchitecture.ARM64 -> "arm64"
            else -> throw ErrorMessageException(I18n.get("unsupported_arch"))
        }
        val osStr = when(OperatingSystem.get()) {
            HostOS.WINDOWS -> "win"
            HostOS.LINUX -> "linux"
            HostOS.MAC -> "osx"
            else ->throw ErrorMessageException(I18n.get("unsupported_os"))
        }

        return getFileBytes("natives/$osStr/$archStr/$fileName") ?: throw Exception("Could not find native $fileName for $osStr/$archStr!")
    }

    fun getSELinuxPolicy() : ByteArray {
        return getFileBytes("selinux/$SELinuxPolicyFileName") ?: throw Exception("Could not find SELinux policy!")
    }

    fun getAppIcon(): Image {
        return ImageIcon(getFileBytes(AppIconFileName)!!).image
    }

    fun getFileBytes(url: String) : ByteArray? {
        val classLoader = ResourceHelper.javaClass.classLoader
        val stream = classLoader.getResourceAsStream(url) ?: return null
        return stream.readBytes()
    }
}
