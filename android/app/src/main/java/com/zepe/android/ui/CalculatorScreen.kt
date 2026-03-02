package com.zepe.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zepe.android.domain.model.MonthMeta
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalculatorScreen(
        state = uiState,
        onSalaryChange = viewModel::onSalaryChange,
        onYearChange = viewModel::onYearChange,
        onCalculate = viewModel::calculate,
    )
}

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onSalaryChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onCalculate: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.salaryInput,
                onValueChange = onSalaryChange,
                label = { Text("Зарплата") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.yearInput,
                onValueChange = onYearChange,
                label = { Text("Год") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) {
                Text("Рассчитать")
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.months) { month ->
                    MonthCard(month)
                }
            }
        }
    }
}

@Composable
private fun MonthCard(month: MonthMeta) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = monthName(month.monthNum),
                style = MaterialTheme.typography.titleMedium,
            )
            PaymentRow("Аванс", month.advanceValue, formatDate(month.advanceDate))
            PaymentRow("Остаток", month.restValue, month.restDate?.let(::formatDate).orEmpty())
        }
    }
}

@Composable
private fun PaymentRow(label: String, value: Int, date: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$label: ${formatMoney(value)}")
        Text(date)
    }
}

private fun monthName(monthNum: Int): String {
    val date = java.time.LocalDate.of(2024, monthNum, 1)
    val formatter = DateTimeFormatter.ofPattern("LLLL", Locale("ru", "RU"))
    return date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }
}

private fun formatDate(date: java.time.LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM, EEE", Locale("ru", "RU"))
    return date.format(formatter)
}

private fun formatMoney(value: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    return formatter.format(value)
}
