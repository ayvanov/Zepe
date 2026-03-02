package com.zepe.android.data.repo

import com.zepe.android.data.api.IsDayOffApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarRepositoryTest {
    @Test
    fun slicesYearDataByMonthAndAppendsNextJanuary() = runBlocking {
        val yearData = buildString {
            repeat(366) { append(if (it % 2 == 0) '0' else '1') }
        }
        val janData = "0000000000000000000000000000000"
        val api = object : IsDayOffApi {
            override suspend fun getData(year: Int, month: Int?): String {
                return if (month == 1 && year == 2027) janData else yearData
            }
        }

        val repository = CalendarRepository(api)
        val slices = repository.fetchYearSlices(2026)

        assertEquals(13, slices.size)
        assertEquals(31, slices[0].length)
        assertEquals(29, slices[1].length)
        assertEquals(31, slices[12].length)
    }
}
