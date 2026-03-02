package com.zepe.android.domain

import com.zepe.android.data.repo.CalendarRepository
import com.zepe.android.domain.model.MonthMeta

class ZepeCalculator(
    private val calendarRepository: CalendarRepository,
) {
    suspend fun getYearData(year: Int, salary: Int): Map<Int, List<MonthMeta>> {
        val actualYear = if (year == 0) java.time.LocalDate.now().year else year
        val slices = calendarRepository.fetchYearSlices(actualYear)
        val months = buildMonths(actualYear, salary, slices)
        return mapOf(actualYear to months)
    }

    fun buildMonths(year: Int, salary: Int, slices: List<String>): List<MonthMeta> {
        val result = mutableListOf<MonthMeta>()
        for (i in 0 until 12) {
            val monthSlice = slices.getOrElse(i) { "" }
            val nextMonthSlice = slices.getOrElse(i + 1) { "" }
            result += MonthMeta(
                monthSlice = monthSlice,
                nextMonthSlice = nextMonthSlice,
                monthNum = i + 1,
                salary = salary,
                year = year,
            )
        }
        return result
    }
}
