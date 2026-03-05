package com.zepe.android.ui

import android.icu.text.CompactDecimalFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zepe.android.data.repo.SettingsRepository
import com.zepe.android.domain.model.MonthMeta
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private data class PaymentEvent(
    val date: LocalDate,
    val amount: Int
)

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModel.Factory(context, settingsRepository)
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
    val scope = rememberCoroutineScope()
    val showBottomSheet = remember { mutableStateOf(false) }

    val locale = remember { Locale.forLanguageTag("ru-RU") }

    val moneyFormatter = remember(locale) {
        CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    }

    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMMM, EEE", locale) }
    val monthNameFormatter = remember(locale) { DateTimeFormatter.ofPattern("LLLL", locale) }

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    val monthMetaMap = remember(state.months) {
        state.months.associateBy { it.year to it.monthNum }
    }

    val groupedPayments = remember(state.months) {
        state.months.flatMap { month ->
            listOfNotNull(
                PaymentEvent(month.advanceDate, month.advanceValue),
                month.restDate?.let { PaymentEvent(it, month.restValue) }
            )
        }
            .groupBy { it.date.year to it.date.monthValue }
            .toList()
            .sortedWith(compareBy({ it.first.first }, { it.first.second }))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet.value = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Настройки")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = groupedPayments,
                    key = { _, group -> "${group.first.first}-${group.first.second}" }
                ) { index, (key, events) ->
                    val isFirst = index == 0
                    val isLast = index == groupedPayments.lastIndex
                    val shape = when {
                        isFirst && isLast -> RoundedCornerShape(16.dp)
                        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RectangleShape
                    }

                    val groupDate = LocalDate.of(key.first, key.second, 1)
                    val monthMeta = monthMetaMap[key]
                    val groupKey = "${key.first}-${key.second}"
                    val isExpanded = expandedStates[groupKey] ?: false

                    MonthCard(
                        monthDate = groupDate,
                        events = events,
                        monthMeta = monthMeta,
                        moneyFormatter = moneyFormatter,
                        dateFormatter = dateFormatter,
                        monthNameFormatter = monthNameFormatter,
                        locale = locale,
                        shape = shape,
                        showDivider = !isLast,
                        isExpanded = isExpanded,
                        onExpandToggle = { expandedStates[groupKey] = !isExpanded }
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }
        }
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false },
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
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet.value = false
                                }
                            }
                        }
                    )
                )

                Button(
                    onClick = {
                        onCalculate()
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet.value = false
                            }
                        }
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
    monthDate: LocalDate,
    events: List<PaymentEvent>,
    monthMeta: MonthMeta?,
    moneyFormatter: CompactDecimalFormat,
    dateFormatter: DateTimeFormatter,
    monthNameFormatter: DateTimeFormatter,
    locale: Locale,
    shape: Shape,
    showDivider: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val now = remember { LocalDate.now() }
    val isCurrentMonth = monthDate.monthValue == now.monthValue && monthDate.year == now.year

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onExpandToggle() },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentMonth) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val monthTitle = remember(monthDate, locale) {
                    monthDate.format(monthNameFormatter).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                    }
                }

                val isPastMonth = monthDate.isBefore(now.withDayOfMonth(1))
                val titleAlpha = if (isPastMonth) 0.6f else 1f

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                events.forEach { event ->
                    PaymentRow(
                        amountText = moneyFormatter.format(event.amount),
                        dateText = event.date.format(dateFormatter),
                        isPast = event.date.isBefore(now)
                    )
                }

                if (isExpanded && monthMeta != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarGrid(monthMeta, events, locale)
                }
            }

            if (showDivider && !isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(monthMeta: MonthMeta, events: List<PaymentEvent>, locale: Locale) {
    val yearMonth = YearMonth.of(monthMeta.year, monthMeta.monthNum)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(monthMeta.year, monthMeta.monthNum, 1)

    // DayOfWeek.value: 1 (Mon) to 7 (Sun)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val emptyCellsBefore = firstDayOfWeek - 1

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )
            weekdays.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days grid
        val totalCells = daysInMonth + emptyCellsBefore
        val rows = (totalCells + 6) / 7
        val vibrantBlue = Color(0xFF007AFF)

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val day = cellIndex - emptyCellsBefore + 1

                    if (cellIndex < emptyCellsBefore || day > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val isDayOff = monthMeta.isDayOff(day)
                        val isPaymentDay = events.any { it.date.dayOfMonth == day }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPaymentDay) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(
                                            width = 1.dp,
                                            color = vibrantBlue,
                                            shape = CircleShape
                                        )
                                )
                            } else if (isDayOff) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                )
                            }
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isPaymentDay) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = when {
                                    isPaymentDay -> vibrantBlue
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    amountText: String,
    dateText: String,
    isPast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseColor = MaterialTheme.colorScheme.onSurface
        val baseDateColor = MaterialTheme.colorScheme.onSurfaceVariant

        val color = if (isPast) baseColor.copy(alpha = 0.3f) else baseColor
        val dateColor = if (isPast) baseDateColor.copy(alpha = 0.3f) else baseDateColor

        Text(
            text = amountText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = dateColor
        )
    }
}
