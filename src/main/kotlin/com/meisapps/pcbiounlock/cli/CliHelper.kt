package com.meisapps.pcbiounlock.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterDescription
import com.meisapps.pcbiounlock.utils.io.Console

object CliHelper {
    fun printHelp() {

        Console.println()
        Console.println("Available app parameters:")
        val appArgs = JCommander.newBuilder()
            .addObject(AppArgs())
            .build()
        appArgs.usageFormatter.usage(StringBuilder())
        printParams(appArgs.parameters)
        Console.println()

        Console.println("Available commands:")
        val cliArgs = JCommander.newBuilder()
            .addObject(CliArgs())
            .build()
        cliArgs.usageFormatter.usage(StringBuilder())
        printParams(cliArgs.parameters)
        Console.println()
    }

    private fun printParams(parameters: List<ParameterDescription>) {
        for(param in parameters) {
            Console.println("${param.names}\t\t\t${param.description}")
        }
    }
}