package com.meisapps.pcbiounlock.cli

import com.beust.jcommander.JCommander
import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.server.pairing.PairingServer
import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.QRGenerator
import kotlin.system.exitProcess


private fun promptUser(): UserData? {
    val native = NativeUtils.getForPlatform()
    val userName = native.getCurrentUserName()

    if(System.console() == null) {
        Console.println("Could not read console.")
        return null
    }

    Console.println()
    Console.println("=> Please enter the password of your user.")
    Console.println("=> Username: $userName")

    print("Password: ")
    val pwd = String(System.console().readPassword())

    if(native.checkUserLogin(userName, pwd)) {
        return UserData(userName, pwd)
    } else {
        Console.println("Invalid password.")
    }
    return null
}

fun runCli(args: Array<String>, shell: Shell) {
    val serviceInstaller = ServiceInstaller.getForPlatform(shell)!!
    val deviceStorage = DeviceStorage.getForPlatform(shell)!!

    Console.println()
    Console.println("== Info ==")
    Console.println("=> Is Installed: ${serviceInstaller.isInstalled()}")
    Console.println("=> Is Paired: ${deviceStorage.getDevices().isNotEmpty()}")
    Console.println()

    var shouldInstall = !serviceInstaller.isInstalled()
    var shouldUninstall = false
    var forcePair = false

    val cliArgs = CliArgs()
    JCommander.newBuilder()
        .addObject(cliArgs)
        .build()
        .parse(*args)

    if(cliArgs.install)
        shouldInstall = true
    if(cliArgs.uninstall)
        shouldUninstall = true
    if(cliArgs.forcePair)
        forcePair = true

    if(shouldInstall && serviceInstaller.isInstalled())
        shouldUninstall = true

    if(shouldUninstall) {
        Console.println("Uninstalling...")
        serviceInstaller.uninstall()
    }
    if(shouldInstall) {
        Console.println("Installing...")
        serviceInstaller.install()
    }

    if(deviceStorage.getDevices().isEmpty() || forcePair) {
        val user = promptUser()
        if(user != null) {
            Console.println("Scan the QR code with the app to pair.")

            val pairingServer = PairingServer(deviceStorage, PairingMethod.TCP, user)
            pairingServer.setOnErrorListener {
                Console.println("Pairing error: $it")
                exitProcess(1)
            }

            QRGenerator.printQRCode(pairingServer.getQRText(), 50, 50)
            pairingServer.runBlocking()
        }
    }

    if(!shouldInstall && !shouldUninstall && !forcePair && deviceStorage.getDevices().isNotEmpty())
        Console.println("Use -h for help.")

    shell.release()
    exitProcess(0)
}
