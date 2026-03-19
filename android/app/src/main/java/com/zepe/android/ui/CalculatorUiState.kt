package com.zepe.android.ui

import com.zepe.android.domain.model.MonthMeta
import java.time.LocalDate

data class UserPayment(
    val date: LocalDate,
    val amount: Double
)

data class CalculatorUiState(
    val salaryInput: String = "",
    val yearInput: String = java.time.LocalDate.now().year.toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val months: List<MonthMeta> = emptyList(),
    val userPayments: List<UserPayment> = emptyList(),
)
