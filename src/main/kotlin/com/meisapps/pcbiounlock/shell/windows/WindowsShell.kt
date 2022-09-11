package com.meisapps.pcbiounlock.shell.windows

import com.meisapps.pcbiounlock.shell.CommandResult
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.io.Console
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.system.exitProcess


class WindowsShell : Shell() {
    override fun isRunningAsAdmin(): Boolean {
        return runUserCommand("net session").exitCode == 0
    }

    override fun restartAsAdmin(args: Array<String>) {
        val jarPath = File(
            Shell::class.java.protectionDomain.codeSource.location
                .toURI()
        ).path

        val isExe = jarPath.endsWith(".exe")
        if(!jarPath.endsWith(".jar") && !isExe) {
            Console.fatal("Please run the app as an administrator.")
            return
        }

        if(isExe) {
            if(runUserCommand("powershell -Command \"Start-Process '$jarPath' -Verb RunAs\"").exitCode != 0) {
                Console.fatal("Please run the app as an administrator.")
                return
            }
        } else {
            val javaExe = System.getProperty("java.home") + "/bin/javaw.exe"
            if(runUserCommand("powershell -Command \"Start-Process '$javaExe' -ArgumentList '-jar \\\"$jarPath\\\"' -Verb RunAs\"").exitCode != 0) {
                Console.fatal("Please run the app as an administrator.")
                return
            }
        }

        exitProcess(0)
    }

    override fun runUserCommand(cmd: String): CommandResult {
        val builder = ProcessBuilder("cmd.exe", "/c", cmd)
        val process = builder.start()

        val stdout: InputStream = process.inputStream
        var output = ""
        val sc = Scanner(stdout)
        while (sc.hasNextLine()) {
            val line = sc.nextLine()
            output += line + "\n"
        }

        val result = CommandResult()
        result.exitCode = process.waitFor()
        result.output = output.trim()
        return result
    }

    override fun acquire() {
        if(!isRunningAsAdmin())
            throw Exception("The application needs to run as an administrator !")
    }

    override fun release() {
    }

    override fun runCommand(cmd: String): CommandResult {
        return runUserCommand(cmd)
    }

    override fun readBytes(path: String): ByteArray? {
        return Files.readAllBytes(Paths.get(path))
    }
    override fun writeBytes(path: String, data: ByteArray) {
        Files.write(Paths.get(path), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}