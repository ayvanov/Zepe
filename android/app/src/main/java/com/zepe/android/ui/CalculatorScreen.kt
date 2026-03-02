package com.zepe.android.ui

import android.icu.text.CompactDecimalFormat
import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zepe.android.data.repo.SettingsRepository
import com.zepe.android.domain.model.MonthMeta
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

    val locale = remember { Locale.forLanguageTag("ru-RU") }
    
    // Оптимизация: Используем CompactDecimalFormat для компактного вывода (доступен с API 24)
    val moneyFormatter = remember(locale) {
        CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    }
    // Получаем символ валюты RUB
    val currencySymbol = remember(locale) {
        Currency.getInstance("RUB").getSymbol(locale)
    }
    
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMMM, EEE", locale) }
    val monthNameFormatter = remember(locale) { DateTimeFormatter.ofPattern("LLLL", locale) }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = state.months,
                    key = { it.monthNum }
                ) { month ->
                    MonthCard(
                        month = month,
                        moneyFormatter = moneyFormatter,
                        currencySymbol = currencySymbol,
                        dateFormatter = dateFormatter,
                        monthNameFormatter = monthNameFormatter,
                        locale = locale
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator()
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
private fun MonthCard(
    month: MonthMeta,
    moneyFormatter: CompactDecimalFormat,
    currencySymbol: String,
    dateFormatter: DateTimeFormatter,
    monthNameFormatter: DateTimeFormatter,
    locale: Locale
) {
    val isCurrentMonth = remember(month.monthNum, month.year) {
        val now = java.time.LocalDate.now()
        month.monthNum == now.monthValue && month.year == now.year
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isCurrentMonth) MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val monthTitle = remember(month.monthNum, locale) {
                val date = java.time.LocalDate.of(java.time.LocalDate.now().year, month.monthNum, 1)
                date.format(monthNameFormatter).replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
                }
            }

            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            PaymentRow(
                valueText = "${moneyFormatter.format(month.advanceValue)} $currencySymbol",
                dateText = month.advanceDate.format(dateFormatter)
            )
            
            PaymentRow(
                valueText = "${moneyFormatter.format(month.restValue)} $currencySymbol",
                dateText = month.restDate?.format(dateFormatter).orEmpty()
            )
        }
    }
}

@Composable
private fun PaymentRow(valueText: String, dateText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = valueText,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(dateText, style = MaterialTheme.typography.bodySmall)
    }
}
