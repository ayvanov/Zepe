package com.zepe.android.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zepe.android.data.api.HttpIsDayOffApi
import com.zepe.android.data.repo.CalendarRepository
import com.zepe.android.data.repo.SettingsRepository
import com.zepe.android.domain.ZepeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalculatorViewModel(
    private val settingsRepository: SettingsRepository,
    private val calculator: ZepeCalculator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedSalary = settingsRepository.salaryFlow.first()
            if (savedSalary.isNotEmpty()) {
                _uiState.update { it.copy(salaryInput = savedSalary) }
                calculate()
            }
        }
    }

    fun onSalaryChange(value: String) {
        _uiState.update { it.copy(salaryInput = value, errorMessage = null) }
    }

    fun addUserPayment(date: LocalDate, amount: Int) {
        _uiState.update { it.copy(userPayments = it.userPayments + UserPayment(date, amount)) }
    }

    fun calculate() {
        val salaryInput = uiState.value.salaryInput
        val salary = salaryInput.toIntOrNull()
        val year = uiState.value.yearInput.toIntOrNull() ?: java.time.LocalDate.now().year
        if (salary == null || salary <= 0) {
            _uiState.update { it.copy(errorMessage = "Введите корректную зарплату") }
            return
        }

        viewModelScope.launch {
            settingsRepository.saveSalary(salaryInput)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                calculator.getYearData(year, salary)
            }.onSuccess { months ->
                _uiState.update { it.copy(isLoading = false, months = months) }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки данных",
                    )
                }
            }
        }
    }

    companion object {
        fun Factory(context: Context, settingsRepository: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val calendarRepo = CalendarRepository(context, HttpIsDayOffApi())
                val calculator = ZepeCalculator(calendarRepo)
                CalculatorViewModel(settingsRepository, calculator)
            }
        }
    }
}
