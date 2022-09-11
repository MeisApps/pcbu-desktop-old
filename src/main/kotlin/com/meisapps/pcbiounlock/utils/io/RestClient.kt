package com.meisapps.pcbiounlock.utils.io

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.URLEncoder


object RestClient {
    private fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    fun get(url: String, queryParams: Map<String, String> = emptyMap()): JsonElement? {
        val urlParams = queryParams.map {(k, v) -> "${(k.utf8())}=${v.utf8()}"}
            .joinToString("&")

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(URI.create("$url?${urlParams}").toURL())
            .build()

        return try {
            val response = client.newCall(request).execute()
            return Json.parseToJsonElement(response.body!!.string())
        } catch (e: Exception) {
            null
        }
    }
}