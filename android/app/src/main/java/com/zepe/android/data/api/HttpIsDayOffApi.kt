package com.zepe.android.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HttpIsDayOffApi : IsDayOffApi {
    override suspend fun getData(year: Int, month: Int?): String = withContext(Dispatchers.IO) {
        val monthQuery = if (month == null) "" else "&month=$month"
        val endpoint = "https://isdayoff.ru/api/getdata?year=$year$monthQuery"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    }
}
