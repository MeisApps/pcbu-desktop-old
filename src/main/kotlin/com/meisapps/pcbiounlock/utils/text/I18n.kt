package com.meisapps.pcbiounlock.utils.text

import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import java.io.StringReader
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle


data class Lang(val name: String, val code: String)

object I18n {
    val languages = listOf(
        Lang("Auto", "auto"),
        Lang("English", "en_US"),
        Lang("Deutsch", "de_DE")
    )

    fun get(key: String): String {
        val bundle = getBundle()
        return try {
            bundle.getString(key)
        } catch (e: Exception) {
            Console.println("Warning: Missing I18n key $key for locale ${Locale.getDefault()}.")
            getDefaultBundle().getString(key)
        }
    }

    fun get(key: String, vararg args: Any?): String {
        val str = get(key)
        return str.format(*args)
    }

    private fun getBundle(): ResourceBundle {
        val bundle = when(AppSettings.get().language) {
            "de_DE" -> getBundle(Locale.GERMANY)
            "en_US" -> getBundle(Locale.US)
            else -> getBundle(Locale.getDefault())
        }

        return bundle ?: getDefaultBundle()
    }

    private fun getDefaultBundle(): ResourceBundle {
        return getBundle(Locale.US)!!
    }

    private fun getBundle(locale: Locale): ResourceBundle? {
        val localeStr = locale.toString()
        val file = ResourceHelper.getFileBytes("i18n/$localeStr.properties") ?: return null

        val fileStr = file.toString(Charsets.ISO_8859_1)
        return PropertyResourceBundle(StringReader(fileStr))
    }
}