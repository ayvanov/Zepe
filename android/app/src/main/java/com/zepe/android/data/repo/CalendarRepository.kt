package com.zepe.android.data.repo

import com.zepe.android.data.api.IsDayOffApi
import java.time.YearMonth

class CalendarRepository(
    private val api: IsDayOffApi,
) {
    suspend fun fetchYearSlices(year: Int): List<String> {
        val yearData = api.getData(year)
        val nextJanuary = api.getData(year + 1, 1)

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
}
