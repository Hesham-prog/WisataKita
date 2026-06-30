package com.wisatakita.app.data.remote

import java.net.HttpURLConnection
import java.net.URL

object ApiHttpClient {
    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) error("HTTP $code: $body")
            body
        } finally {
            connection.disconnect()
        }
    }
}
