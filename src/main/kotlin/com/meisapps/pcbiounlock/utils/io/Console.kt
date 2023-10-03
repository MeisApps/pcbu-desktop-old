package com.meisapps.pcbiounlock.utils.io

import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GraphicsEnvironment
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.swing.JOptionPane
import kotlin.system.exitProcess


object Console {
    var enableLogging = false
    var isHeadless = GraphicsEnvironment.isHeadless()

    fun println() {
        kotlin.io.println()
        System.out.flush()
        if(enableLogging)
            logMsg(System.lineSeparator())
    }

    fun println(message: String, shouldLog: Boolean = true) {
        kotlin.io.println(message)
        System.out.flush()
        if(enableLogging && shouldLog)
            logMsg(message + System.lineSeparator())
    }

    fun fatal(message: String) {
        kotlin.io.println(message)
        System.out.flush()

        if(!isHeadless) {
            JOptionPane.showMessageDialog(null, message, I18n.get("fatal_error"), JOptionPane.ERROR_MESSAGE)
        }

        if(enableLogging)
            logMsg(message + System.lineSeparator())

        exitProcess(1)
    }

    private fun logMsg(message: String) {
        Files.write(Paths.get("PCBioUnlock.log"), message.toByteArray(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }
}