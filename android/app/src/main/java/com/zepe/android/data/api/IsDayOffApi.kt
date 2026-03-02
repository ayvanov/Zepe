package com.zepe.android.data.api

interface IsDayOffApi {
    suspend fun getData(year: Int, month: Int? = null): String
}
