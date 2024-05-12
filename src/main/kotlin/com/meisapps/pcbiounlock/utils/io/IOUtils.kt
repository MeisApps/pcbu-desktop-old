package com.meisapps.pcbiounlock.utils.io

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object IOUtils {
    fun findLine(file: File, lineStr: String) : Boolean {
        if(!file.exists())
            return false
        val bufReader = BufferedReader(FileReader(file))
        while(true) {
            val line = bufReader.readLine() ?: break
            if(line == lineStr)
                return true
        }

        return false
    }
}
