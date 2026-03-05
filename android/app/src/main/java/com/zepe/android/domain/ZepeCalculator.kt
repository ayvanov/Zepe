package com.zepe.android.domain

import com.zepe.android.data.repo.CalendarRepository
import com.zepe.android.domain.model.MonthMeta

class ZepeCalculator(
    private val calendarRepository: CalendarRepository,
) {
    suspend fun getYearData(year: Int, salary: Int): List<MonthMeta> {
        val actualYear = if (year == 0) java.time.LocalDate.now().year else year
        val slices = calendarRepository.fetchYearSlices(actualYear)

        val result = mutableListOf<MonthMeta>()

        // 12 месяцев текущего года
        for (i in 0 until 12) {
            result += MonthMeta(
                monthSlice = slices.getOrElse(i) { "" },
                nextMonthSlice = slices.getOrElse(i + 1) { "" }, // Для декабря берется срез января
                monthNum = i + 1,
                salary = salary,
                year = actualYear,
            )
        }

        // Январь следующего года
        if (slices.size > 12) {
            result += MonthMeta(
                monthSlice = slices[12],
                nextMonthSlice = "", // Данных за февраль нет
                monthNum = 1,
                salary = salary,
                year = actualYear + 1,
            )
        }

        return result
    }
}
