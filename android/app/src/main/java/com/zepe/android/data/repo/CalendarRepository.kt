package com.zepe.android.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zepe.android.data.api.IsDayOffApi
import kotlinx.coroutines.flow.first
import java.time.YearMonth

private val Context.calendarDataStore by preferencesDataStore(name = "calendar_cache")

class CalendarRepository(
    private val context: Context,
    private val api: IsDayOffApi,
) {
    companion object {
        private const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 1 день
    }

    suspend fun fetchYearSlices(year: Int): List<String> {
        val yearData = getOrFetch(year.toString()) { api.getData(year) }
        val nextJanuary = getOrFetch("${year + 1}_1") { api.getData(year + 1, 1) }

        if (yearData.isEmpty()) {
            throw IllegalStateException("Failed to fetch Year Data")
        }

        val slices = mutableListOf<String>()
        var offset = 0
        for (month in 1..12) {
            val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
            slices += yearData.substring(offset, offset + daysInMonth)
            offset += daysInMonth
        }
        if (nextJanuary.isNotEmpty()) {
            slices += nextJanuary
        }
        return slices
    }

    private suspend fun getOrFetch(key: String, fetcher: suspend () -> String): String {
        val prefKey = stringPreferencesKey(key)
        val timeKey = longPreferencesKey("${key}_timestamp")
        
        val preferences = context.calendarDataStore.data.first()
        val cached = preferences[prefKey]
        val timestamp = preferences[timeKey] ?: 0L
        
        val now = System.currentTimeMillis()
        val isExpired = now - timestamp > CACHE_EXPIRATION_MS

        // Если есть актуальный кэш — возвращаем его
        if (cached != null && cached.isNotEmpty() && !isExpired) {
            return cached
        }

        return try {
            val remote = fetcher()
            if (remote.isNotEmpty()) {
                context.calendarDataStore.edit { prefs ->
                    prefs[prefKey] = remote
                    prefs[timeKey] = now
                }
                remote
            } else {
                cached ?: ""
            }
        } catch (_: Exception) {
            // Если запрос не удался (нет интернета), возвращаем старый кэш
            cached ?: ""
        }
    }
}
