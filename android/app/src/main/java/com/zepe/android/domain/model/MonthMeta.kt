package com.zepe.android.domain.model

import java.time.LocalDate
import kotlin.math.roundToInt

class MonthMeta(
    private val monthSlice: String,
    private val nextMonthSlice: String,
    val monthNum: Int,
    val salary: Int,
    val year: Int,
) {
    companion object {
        const val salaryMultiplier: Double = 1.0
        const val advanceDays: Int = 15
        const val advancePayDay: Int = 25
        const val restPayDay: Int = 10
    }

    private val advanceSlice: String = monthSlice.take(advanceDays)

    val total: Int
        get() = monthSlice.length

    val workdays: Int
        get() = monthSlice.count { it == '0' || it == '2' }

    val holidays: Int
        get() = monthSlice.count { it == '1' }

    val salaryPerDay: Int
        get() {
            if (workdays == 0) return 0
            return ((salary.toDouble() / workdays.toDouble()) * salaryMultiplier).roundToInt()
        }

    val advanceWorkdays: Int
        get() = advanceSlice.count { it == '0' || it == '2' }

    val advanceValue: Int
        get() = (salaryPerDay * advanceWorkdays.toDouble()).roundToInt()

    val advanceDate: LocalDate
        get() {
            val limit = advancePayDay.coerceAtMost(monthSlice.length)
            val dayIndex = monthSlice.take(limit).lastIndexOfAny(charArrayOf('0', '2'))
            val finalDay = if (dayIndex == -1) 1 else dayIndex + 1
            return LocalDate.of(year, monthNum, finalDay)
        }

    val restValue: Int
        get() = salary - advanceValue

    val restDate: LocalDate?
        get() {
            if (nextMonthSlice.isEmpty()) return null
            val limit = restPayDay.coerceAtMost(nextMonthSlice.length)
            val dayIndex = nextMonthSlice.take(limit).lastIndexOfAny(charArrayOf('0', '2'))
            
            return if (dayIndex == -1) {
                // Если в начале месяца нет рабочих дней (длинные праздники), 
                // выплата переносится на ПОСЛЕДНИЙ рабочий день ПРЕДЫДУЩЕГО месяца
                val lastWorkdayInCurrent = monthSlice.lastIndexOfAny(charArrayOf('0', '2'))
                if (lastWorkdayInCurrent != -1) {
                    LocalDate.of(year, monthNum, lastWorkdayInCurrent + 1)
                } else {
                    // Крайний случай (не должно быть)
                    LocalDate.of(year, monthNum, 1).plusMonths(1).withDayOfMonth(1)
                }
            } else {
                LocalDate.of(year, monthNum, 1).plusMonths(1).withDayOfMonth(dayIndex + 1)
            }
        }

    fun isDayOff(day: Int): Boolean = monthSlice.getOrNull(day - 1) == '1'
    
    fun isPreHoliday(day: Int): Boolean = monthSlice.getOrNull(day - 1) == '2'
}
