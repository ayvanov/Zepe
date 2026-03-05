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
        get() = monthSlice.count { it == '0' }

    val holidays: Int
        get() = monthSlice.count { it == '1' }

    val salaryPerDay: Int
        get() {
            if (workdays == 0) return 0
            return ((salary.toDouble() / workdays.toDouble()) * salaryMultiplier).roundToInt()
        }

    val advanceWorkdays: Int
        get() = advanceSlice.count { it == '0' }

    val advanceValue: Int
        get() = (salaryPerDay * advanceWorkdays.toDouble()).roundToInt()

    val advanceDate: LocalDate
        get() {
            val day = monthSlice.take(advancePayDay).lastIndexOf('0') + 1
            return LocalDate.of(year, monthNum, day.coerceAtLeast(1))
        }

    val restValue: Int
        get() = salary - advanceValue

    val restDate: LocalDate?
        get() {
            if (nextMonthSlice.isEmpty()) return null
            val day = nextMonthSlice.take(restPayDay).lastIndexOf('0') + 1
            return LocalDate.of(year, monthNum, 1).plusMonths(1).withDayOfMonth(day.coerceAtLeast(1))
        }

    fun isDayOff(day: Int): Boolean = monthSlice.getOrNull(day - 1) == '1'
}
