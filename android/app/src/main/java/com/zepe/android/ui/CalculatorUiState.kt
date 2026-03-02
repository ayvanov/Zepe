package com.zepe.android.ui

import com.zepe.android.domain.model.MonthMeta

data class CalculatorUiState(
    val salaryInput: String = "",
    val yearInput: String = java.time.LocalDate.now().year.toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val months: List<MonthMeta> = emptyList(),
)
