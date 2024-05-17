package com.meisapps.pcbiounlock

import com.formdev.flatlaf.FlatDarkLaf
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GraphicsEnvironment
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile
import kotlin.io.path.absolutePathString

object InitClassLoader {
    @JvmStatic
    fun main(args: Array<String>) {
        if(!GraphicsEnvironment.isHeadless())
            FlatDarkLaf.setup()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Console.println(e.stackTraceToString())
            Console.fatal(I18n.get("unknown_error"))
        }

        try {
            var isBcLoadable = false
            try {
                InitClassLoader::class.java.classLoader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider")
                isBcLoadable = true
            } catch (_: Exception) {}

            var classPath = ""
            val jarFile = File(InitClassLoader::class.java.protectionDomain.codeSource.location.toURI())
            if (jarFile.isFile && !isBcLoadable) {
                JarFile(jarFile).use { jar ->
                    val entries = jar.entries()
                    val jarDirectory = File(jarFile.parentFile, "PCBioUnlock")
                    jarDirectory.mkdirs()

                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name.startsWith("libs/") && entry.name.endsWith(".jar")) {
                            val libFile = File(jarDirectory, File(entry.name).name)
                            if(libFile.exists())
                                continue
                            jar.getInputStream(entry).use { inputStream ->
                                Files.copy(inputStream, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                            }
                        }
                    }

                    classPath = Paths.get(jarDirectory.absolutePath, "bcprov-jdk18on-1.78.1.jar").absolutePathString()
                    val shell = Shell.getForPlatform()!!
                    shell.restartAsAdmin(args, classPath)
                }
            }

            val appClass = InitClassLoader::class.java.classLoader.loadClass("com.meisapps.pcbiounlock.MainKt")
            appClass.getMethod("runMain", Array<String>::class.java).invoke(null, args as Any)
        } catch(e: NoClassDefFoundError) {
            Console.println(e.stackTraceToString())
            Console.fatal(I18n.get("error_class_path"))
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
            Console.fatal(e.message ?: I18n.get("unknown_error"))
        } catch (error: Error) {
            Console.println(error.stackTraceToString())
            Console.fatal(I18n.get("unknown_error"))
        }
    }
}
