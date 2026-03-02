package com.zepe.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zepe.android.data.api.HttpIsDayOffApi
import com.zepe.android.data.repo.CalendarRepository
import com.zepe.android.domain.ZepeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val calculator: ZepeCalculator = ZepeCalculator(CalendarRepository(HttpIsDayOffApi())),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onSalaryChange(value: String) {
        _uiState.update { it.copy(salaryInput = value, errorMessage = null) }
    }

    fun onYearChange(value: String) {
        _uiState.update { it.copy(yearInput = value, errorMessage = null) }
    }

    fun calculate() {
        val salary = uiState.value.salaryInput.toIntOrNull()
        val year = uiState.value.yearInput.toIntOrNull() ?: java.time.LocalDate.now().year
        if (salary == null || salary <= 0) {
            _uiState.update { it.copy(errorMessage = "Введите корректную зарплату") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                calculator.getYearData(year, salary)
            }.onSuccess { data ->
                val months = data[year] ?: data.values.firstOrNull().orEmpty()
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
}
