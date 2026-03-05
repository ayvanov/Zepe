package com.zepe.android.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zepe.android.data.api.IsDayOffApi
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
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
        var nextJanuary = getOrFetch("${year + 1}_1") { api.getData(year + 1, 1) }

        if (yearData.isEmpty()) {
            throw IllegalStateException("Failed to fetch Year Data")
        }

        // Если API вернул только нули для следующего января (нет официальных данных),
        // используем структуру праздников текущего года + стандартные выходные
        if (nextJanuary.isNotEmpty() && nextJanuary.all { it == '0' }) {
            val currentJan = yearData.take(31)
            val holidaysAtStart = currentJan.takeWhile { it == '1' }.length
            
            val nextJanBuilt = StringBuilder()
            val nextYear = year + 1
            for (day in 1..31) {
                if (day <= holidaysAtStart) {
                    nextJanBuilt.append('1')
                } else {
                    val date = LocalDate.of(nextYear, 1, day)
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                    nextJanBuilt.append(if (isWeekend) '1' else '0')
                }
            }
            nextJanuary = nextJanBuilt.toString()
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
