package com.wisatakita.app.data.remote

import java.net.HttpURLConnection
import java.net.URL

class ApiHttpException(
    val statusCode: Int,
    val responseBody: String
) : Exception("HTTP $statusCode: $responseBody")

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
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw ApiHttpException(code, body)
            body
        } finally {
            connection.disconnect()
        }
    }
}
