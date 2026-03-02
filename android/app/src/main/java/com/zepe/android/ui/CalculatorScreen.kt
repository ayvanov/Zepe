package com.zepe.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zepe.android.data.repo.SettingsRepository
import com.zepe.android.domain.model.MonthMeta
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModel.Factory(settingsRepository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalculatorScreen(
        state = uiState,
        onSalaryChange = viewModel::onSalaryChange,
        onCalculate = viewModel::calculate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onSalaryChange: (String) -> Unit,
    onCalculate: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Настройки")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.salaryInput,
                    onValueChange = onSalaryChange,
                    label = { Text("Зарплата") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onCalculate()
                            showBottomSheet = false
                        }
                    )
                )

                Button(
                    onClick = {
                        onCalculate()
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Рассчитать")
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            PaymentRow("Аванс: ${formatMoney(month.advanceValue)}", formatDate(month.advanceDate))
            PaymentRow("Остаток: ${formatMoney(month.restValue)}", month.restDate?.let(::formatDate).orEmpty())
        }
    }
}

@Composable
private fun PaymentRow(labelText: String, date: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(labelText)
        Text(date)
    }
}

private fun monthName(monthNum: Int): String {
    val date = java.time.LocalDate.of(java.time.LocalDate.now().year, monthNum, 1)
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
