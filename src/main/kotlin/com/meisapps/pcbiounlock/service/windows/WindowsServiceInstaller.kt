package com.meisapps.pcbiounlock.service.windows

import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.host.HostArchitecture
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.io.ResourceHelper.WinARM64CryptoFileName
import com.meisapps.pcbiounlock.utils.io.ResourceHelper.WinARM64SSLFileName
import com.meisapps.pcbiounlock.utils.io.ResourceHelper.WinModuleFileName
import com.meisapps.pcbiounlock.utils.io.ResourceHelper.WinX64CryptoFileName
import com.meisapps.pcbiounlock.utils.io.ResourceHelper.WinX64SSLFileName
import java.io.File

class WindowsServiceInstaller(private val shell: Shell) : ServiceInstaller() {
    companion object {
        const val ProviderName = "win-pcbiounlock"
        const val ProviderDllGuid = "74A23DE2-B81D-46EC-E129-CD32507ED716"

        const val JavaFirewallRule = "Java (PC Bio Unlock)"
        const val LogonUIFirewallRule = "LogonUI (PC Bio Unlock)"
    }
    private val isArm = HostUtils.getCpuArchitecture() == HostArchitecture.ARM64

    override fun getModulePath(): String {
        return "${getSystem32()}$WinModuleFileName"
    }

    override fun isInstalled(): Boolean {
        return File(getModulePath()).exists() && isOpenSSLInstalled()
    }

    override fun installOpenSSL() {
        Console.println("Installing OpenSSL...")
        val sslData = ResourceHelper.getNativeByName(if(isArm) WinARM64SSLFileName else WinX64SSLFileName)
        shell.writeBytes(getSystem32() + if(isArm) WinARM64SSLFileName else WinX64SSLFileName, sslData)

        val cryptoData = ResourceHelper.getNativeByName(if(isArm) WinARM64CryptoFileName else WinX64CryptoFileName)
        shell.writeBytes(getSystem32() + if(isArm) WinARM64CryptoFileName else WinX64CryptoFileName, cryptoData)
    }

    override fun doInstall() {
        Console.println("Copying credential provider...")
        val dllData = ResourceHelper.getNativeByName(WinModuleFileName)
        shell.writeBytes("${getSystem32()}$WinModuleFileName", dllData)

        Console.println("Creating registry entries...")
        val regResult =
            shell.runCommand("reg add \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Authentication\\Credential Providers\\{$ProviderDllGuid}\" /t REG_SZ /d $ProviderName /f").exitCode == 0 &&
            shell.runCommand("reg add \"HKEY_CLASSES_ROOT\\CLSID\\{$ProviderDllGuid}\" /t REG_SZ /d $ProviderName /f").exitCode == 0 &&
            shell.runCommand("reg add \"HKEY_CLASSES_ROOT\\CLSID\\{$ProviderDllGuid}\\InprocServer32\" /t REG_SZ /d $ProviderName /f").exitCode == 0 &&
            shell.runCommand("reg add \"HKEY_CLASSES_ROOT\\CLSID\\{$ProviderDllGuid}\\InprocServer32\" /t REG_SZ /v ThreadingModel /d Apartment /f").exitCode == 0
        if(!regResult)
            throw Exception("Could not create registry keys !")

        Console.println("Adding firewall rules...")
        /*val fwResult = shell.runCommand("netsh advfirewall firewall add rule name=\"$LogonUIFirewallRule\" dir=in program=\"c:\\windows\\system32\\LogonUI.exe\" profile=any action=allow").exitCode == 0
        if(!fwResult)
            Console.println("Failed to add LogonUI firewall rule !")*/

        var javaExe = System.getProperty("java.home") + "\\bin\\javaw.exe"
        javaExe = javaExe.replace('/', '\\')
        val fwClearResult = shell.runCommand("powershell /c \"Get-NetFirewallRule | Where-Object { \$_.Direction -eq \\\"Inbound\\\" -or \$_.Direction -eq \\\"Outbound\\\" } | Get-NetFirewallApplicationFilter | Where-Object { \$_.Program -eq \\\"$javaExe\\\" } | Remove-NetFirewallRule\"").exitCode == 0
        if(!fwClearResult)
            Console.println("Failed to clear old Java firewall rules !")
        val javaFwResult = shell.runCommand("netsh advfirewall firewall add rule name=\"$JavaFirewallRule\" dir=in program=\"$javaExe\" profile=any action=allow").exitCode == 0
        if(!javaFwResult)
            Console.println("Failed to add Java firewall rule !")
    }

    override fun doUninstall(fullUninstall: Boolean) {
        Console.println("Removing credential provider...")
        val path = getModulePath().replace('/', '\\')
        shell.runCommand("del $path")
        if(File(path).exists())
            throw Exception("Could not remove credential provider !")

        Console.println("Removing registry entries...")
        val regResult =
            shell.runCommand("reg delete \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Authentication\\Credential Providers\\{$ProviderDllGuid}\" /f").exitCode == 0 &&
            shell.runCommand("reg delete \"HKEY_CLASSES_ROOT\\CLSID\\{$ProviderDllGuid}\" /f").exitCode == 0
        if(!regResult)
            Console.println("Could not delete registry keys !")

        Console.println("Removing firewall rules...")
        /*if(shell.runCommand("netsh advfirewall firewall delete rule name=\"$LogonUIFirewallRule\"").exitCode != 0)
            Console.println("Could not remove LogonUI firewall rule !")*/
        if(shell.runCommand("netsh advfirewall firewall delete rule name=\"$JavaFirewallRule\"").exitCode != 0)
            Console.println("Could not remove Java firewall rule !")
    }

    override fun isOpenSSLInstalled(): Boolean {
        return File(getSystem32() + if(isArm) WinARM64SSLFileName else WinX64SSLFileName).exists() &&
                File(getSystem32() + if(isArm) WinARM64CryptoFileName else WinX64CryptoFileName).exists()
    }

    private fun getSystem32(): String {
        return "C:/Windows/System32/"
    }
}
