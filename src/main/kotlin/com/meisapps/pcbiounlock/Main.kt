package com.meisapps.pcbiounlock

import com.beust.jcommander.JCommander
import com.formdev.flatlaf.FlatDarkLaf
import com.google.common.net.InetAddresses
import com.meisapps.pcbiounlock.cli.AppArgs
import com.meisapps.pcbiounlock.cli.CliHelper
import com.meisapps.pcbiounlock.cli.runCli
import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.service.api.BluetoothApi
import com.meisapps.pcbiounlock.service.api.PCBUApi
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.storage.PCBUAppSettings
import com.meisapps.pcbiounlock.updater.AppUpdater
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.security.Security
import kotlin.system.exitProcess


private fun createAndShowGUI(showSetup: Boolean) {
    val frame = if(showSetup) SetupFrame() else MainFrame()
    frame.isVisible = true
}

fun runMain(args: Array<String>) {
    // Init
    Security.addProvider(BouncyCastleProvider())
    StartupHelper.verifyEnvironment()

    // Args
    val shell = Shell.getForPlatform()!!
    val serviceInstaller = ServiceInstaller.getForPlatform(shell)!!
    val deviceStorage = DeviceStorage.getForPlatform(shell)!!

    var shouldRunCli = GraphicsEnvironment.isHeadless()
    var shouldRunSetup = !serviceInstaller.isInstalled()

    val appArgs = AppArgs()
    JCommander.newBuilder()
        .addObject(appArgs)
        .build()
        .parse(*args)

    if(appArgs.showHelp) {
        CliHelper.printHelp()
        exitProcess(0)
    }
    if(appArgs.forcedIp != null && !InetAddresses.isInetAddress(appArgs.forcedIp!!)) {
        Console.fatal("Invalid IP address ${appArgs.forcedIp}")
        return
    }
    if(appArgs.runCli)
        shouldRunCli = true
    if(appArgs.runSetup)
        shouldRunSetup = true

    Console.enableLogging = !shouldRunCli
    Console.isHeadless = shouldRunCli

    // Check for updates
    AppUpdater.checkForUpdates(shouldRunCli)

    // Check admin
    if(!shell.isRunningAsAdmin()) {
        shell.restartAsAdmin(args, "")
    }
    try {
        shell.acquire()
    } catch (e: Exception) {
        Console.fatal(I18n.get("error_elevate_failed"))
        return
    }

    // Run
    AppSettings.init()
    deviceStorage.init()
    if(appArgs.forcedIp != null) {
        val settings = AppSettings.get()
        AppSettings.set(PCBUAppSettings(settings.installedVersion, settings.language, appArgs.forcedIp!!, settings.unlockServerPort, settings.pairingServerPort, settings.waitForKeyPress))
    }

    Console.println("=== PC Bio Unlock (v${VersionInfo.getAppVersion()}) ===")
    Console.println("=> CPU: ${HostUtils.getCpuArchitecture()}")
    Console.println("=> IP: ${PCBUApi.getLocalIP()}")
    Console.println("=> MAC: ${PCBUApi.getMacAddress()}", false)
    Console.println("=> Device Name: ${HostUtils.getDeviceName()}")
    Console.println("=> Device ID: ${NativeUtils.getForPlatform().getDeviceUUID()}", false)
    Console.println("=> Has Bluetooth: ${BluetoothApi.isBluetoothAvailable()}")
    Console.println("=> Current User: ${NativeUtils.getForPlatform().getCurrentUserName()}")
    Console.println()

    // Update installed module
    AppUpdater.updateNatives(shouldRunCli)

    if(shouldRunCli) {
        runCli(args, shell)
    } else {
        EventQueue.invokeLater { createAndShowGUI(shouldRunSetup) }
    }
}
