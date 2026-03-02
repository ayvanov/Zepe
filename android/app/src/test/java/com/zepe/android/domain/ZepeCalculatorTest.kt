package com.zepe.android.domain

import com.zepe.android.domain.model.MonthMeta
import org.junit.Assert.assertEquals
import org.junit.Test

class ZepeCalculatorTest {
    @Test
    fun monthMetaCalculatesSalaryValuesAndDates() {
        val monthSlice = "0000000000000001111111111111111"
        val nextMonthSlice = "1111000011110000111100001111000"
        val meta = MonthMeta(
            monthSlice = monthSlice,
            nextMonthSlice = nextMonthSlice,
            monthNum = 1,
            salary = 100000,
            year = 2026,
        )

        assertEquals(15, meta.workdays)
        assertEquals(6667, meta.salaryPerDay)
        assertEquals(100005, meta.advanceValue)
        assertEquals(-5, meta.restValue)
        assertEquals(15, meta.advanceDate.dayOfMonth)
        assertEquals(8, meta.restDate?.dayOfMonth)
    }

    @Test
    fun zeroWorkdaysReturnsZeroSalaryPerDay() {
        val meta = MonthMeta(
            monthSlice = "1111111111111111111111111111111",
            nextMonthSlice = "0000000000000000000000000000000",
            monthNum = 1,
            salary = 100000,
            year = 2026,
        )

        assertEquals(0, meta.salaryPerDay)
        assertEquals(0, meta.advanceValue)
    }
}
