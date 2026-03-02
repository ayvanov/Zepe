package com.zepe.android.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val salaryKey = stringPreferencesKey("salary")

    val salaryFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[salaryKey] ?: ""
    }

    suspend fun saveSalary(salary: String) {
        context.dataStore.edit { preferences ->
            preferences[salaryKey] = salary
        }
    }
}
