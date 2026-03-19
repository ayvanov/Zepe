package com.zepe.android.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zepe.android.ui.UserPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val salaryKey = stringPreferencesKey("salary")
    private val userPaymentsKey = stringPreferencesKey("user_payments")

    val salaryFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[salaryKey] ?: ""
    }

    val userPaymentsFlow: Flow<List<UserPayment>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[userPaymentsKey] ?: "[]"
        try {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                UserPayment(
                    date = LocalDate.parse(obj.getString("date")),
                    amount = obj.getDouble("amount")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveSalary(salary: String) {
        context.dataStore.edit { preferences ->
            preferences[salaryKey] = salary
        }
    }

    suspend fun saveUserPayments(payments: List<UserPayment>) {
        val jsonArray = JSONArray()
        payments.forEach { payment ->
            val obj = JSONObject()
            obj.put("date", payment.date.toString())
            obj.put("amount", payment.amount)
            jsonArray.put(obj)
        }
        context.dataStore.edit { preferences ->
            preferences[userPaymentsKey] = jsonArray.toString()
        }
    }
}
