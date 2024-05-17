package com.meisapps.pcbiounlock.shell.linux

import com.google.common.io.BaseEncoding
import com.meisapps.pcbiounlock.shell.CommandResult
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.io.Console
import java.awt.GraphicsEnvironment
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class LinuxShell : Shell() {
    private lateinit var process: Process
    private lateinit var cmdWriter: BufferedWriter
    private lateinit var outThread: Thread

    private val lastExitCode = AtomicInteger(-1)
    private var lastProcOutput = ""

    override fun isRunningAsAdmin(): Boolean {
        val user = runUserCommand("id -u").output
        return user.toInt() == 0
    }

    override fun restartAsAdmin(args: Array<String>, classPath: String) {
        val jarPath = File(
            Shell::class.java.protectionDomain.codeSource.location
                .toURI()
        ).path

        if(!jarPath.endsWith(".jar"))
            return

        if(classPath.isNotBlank()) {
            val isOldJava = System.getProperty("java.version").startsWith("1.")
            val javaExe = System.getProperty("java.home") + "/bin/java"
            val javaArgs = if(isOldJava)
                "-cp '$classPath' -p '$classPath' -jar '$jarPath'"
            else
                "-cp '$classPath' -jar '$jarPath'"
            val builder = ProcessBuilder("bash", "-c", "'$javaExe' $javaArgs")
            builder.start()
            exitProcess(0)
        }

        if(GraphicsEnvironment.isHeadless()) {
            Console.fatal("Please run as root.\nExample: sudo java -jar PCBioUnlock.jar")
            return
        }
    }

    override fun runUserCommand(cmd: String): CommandResult {
        val builder = ProcessBuilder("bash", "-c", cmd)
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
        if(hasShell || isRunningAsAdmin())
            return

        val builder = if(GraphicsEnvironment.isHeadless())
                            ProcessBuilder("sudo", "bash")
                        else
                            ProcessBuilder("pkexec", "bash")
        process = builder.start()

        val stdin: OutputStream = process.outputStream
        val stdout: InputStream = process.inputStream

        cmdWriter = BufferedWriter(OutputStreamWriter(stdin))
        outThread = Thread {
            val sc = Scanner(stdout)
            while (sc.hasNextLine()) {
                val line = sc.nextLine()
                try {
                    var exitCode = Integer.parseInt(line)
                    if(exitCode == -1)
                        exitCode = -2
                    lastExitCode.set(exitCode)
                } catch (_: Exception) {
                    //println(line)
                    lastProcOutput += line + "\n"
                }
            }
        }
        outThread.start()
        hasShell = true

        if(runCommand("echo init").exitCode != 0)
            throw Exception("Could not acquire shell.")
    }

    override fun release() {
        if(!hasShell || isRunningAsAdmin())
            return

        cmdWriter.close()
        outThread.join()
        process.waitFor()
        hasShell = false
    }

    override fun runCommand(cmd: String): CommandResult {
        if(isRunningAsAdmin())
            return runUserCommand(cmd)
        if(!hasShell)
            throw Exception("Shell is released !")

        lastExitCode.set(-1)
        lastProcOutput = ""

        val fullCmd = "$cmd; echo $?\n"
        cmdWriter.write(fullCmd)
        cmdWriter.flush()

        var exitCode: Int
        val output: String
        val startTime = System.currentTimeMillis()
        while (true) {
            exitCode = lastExitCode.get()
            if(exitCode != -1) {
                output = lastProcOutput
                lastProcOutput = ""
                break
            }

            if(!process.isAlive) {
                throw Exception("Could not acquire shell.")
            }
            if(System.currentTimeMillis() - startTime > 30000) {
                throw Exception("Command timed out.")
            }
            Thread.sleep(10)
        }

        val result = CommandResult()
        result.exitCode = exitCode
        result.output = output
        return result
    }

    override fun readBytes(path: String): ByteArray? {
        if(isRunningAsAdmin())
            return Files.readAllBytes(Paths.get(path))

        val result = runCommand("od -t x1 -A n -v < \"$path\" | tr -dc '[:xdigit:]' && echo")
        if(result.exitCode != 0)
            return null

        return try {
            BaseEncoding.base16().decode(result.output.trim().uppercase())
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
            null
        }
    }
    override fun writeBytes(path: String, data: ByteArray) {
        if(isRunningAsAdmin()) {
            Files.write(Paths.get(path), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            return
        }

        val HEXES = "0123456789ABCDEF"
        val hex: StringBuilder = StringBuilder(4 * data.size)
        for (b in data) {
            hex.append("\\x")
            hex.append(HEXES[b.toInt() and 0xF0 shr 4]).append(HEXES[b.toInt() and 0x0F])
        }

        if(runCommand("echo -n -e '$hex' > \"$path\"").exitCode != 0)
            throw Exception("Failed to write bytes !")
    }
}
