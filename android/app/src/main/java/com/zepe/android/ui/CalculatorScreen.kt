package com.zepe.android.ui

import android.icu.text.CompactDecimalFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private data class PaymentEvent(
    val date: LocalDate,
    val amount: Double,
    val userPayment: UserPayment? = null
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
        onAddUserPayment = viewModel::addUserPayment,
        onEditUserPayment = viewModel::editUserPayment,
        onDeleteUserPayment = viewModel::deleteUserPayment
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onSalaryChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onAddUserPayment: (LocalDate, Double) -> Unit,
    onEditUserPayment: (UserPayment, UserPayment) -> Unit,
    onDeleteUserPayment: (UserPayment) -> Unit
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
    var isRefreshing by remember { mutableStateOf(false) }

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<UserPayment?>(null) }
    var targetMonthForNewPayment by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    val monthMetaMap = remember(state.months) {
        state.months.associateBy { it.year to it.monthNum }
    }

    val groupedPayments = remember(state.months, state.userPayments) {
        val autoPayments = state.months.flatMap { month ->
            listOfNotNull(
                PaymentEvent(month.advanceDate, month.advanceValue.toDouble()),
                month.restDate?.let { PaymentEvent(it, month.restValue.toDouble()) }
            )
        }
        val userEvents = state.userPayments.map { PaymentEvent(it.date, it.amount, it) }

        (autoPayments + userEvents)
            .groupBy { it.date.year to it.date.monthValue }
            .toList()
            .sortedWith(compareBy({ it.first.first }, { it.first.second }))
            .map { (key, events) -> key to events.sortedBy { it.date } }
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    val anyExpanded = expandedStates.values.any { it }
                    if (anyExpanded) {
                        expandedStates.clear()
                    } else {
                        groupedPayments.forEach { (key, _) ->
                            expandedStates["${key.first}-${key.second}"] = true
                        }
                    }
                    delay(500)
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(padding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                            onExpandToggle = { expandedStates[groupKey] = !isExpanded },
                            onAddPaymentClick = {
                                editingPayment = null
                                targetMonthForNewPayment = groupDate
                                showAddPaymentDialog = true
                            },
                            onEditUserPayment = { payment ->
                                editingPayment = payment
                                showAddPaymentDialog = true
                            },
                            onDeleteUserPayment = { payment ->
                                scope.launch {
                                    onDeleteUserPayment(payment)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Выплата удалена",
                                        actionLabel = "Отмена",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onAddUserPayment(payment.date, payment.amount)
                                    }
                                }
                            }
                        )

                        val now = LocalDate.now()
                        if (groupDate.monthValue == 12 && groupDate.year == now.year) {
                            val yearTotal = groupedPayments
                                .filter { it.first.first == now.year }
                                .flatMap { it.second }
                                .sumOf { it.amount }
                            
                            YearTotalCard(
                                totalAmount = yearTotal,
                                moneyFormatter = moneyFormatter
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            initialDate = editingPayment?.date ?: targetMonthForNewPayment ?: LocalDate.now(),
            initialAmount = editingPayment?.amount,
            onDismiss = { 
                showAddPaymentDialog = false
                editingPayment = null
            },
            onConfirm = { date, amount ->
                if (editingPayment != null) {
                    onEditUserPayment(editingPayment!!, UserPayment(date, amount))
                } else {
                    onAddUserPayment(date, amount)
                }
                showAddPaymentDialog = false
                editingPayment = null
            }
        )
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false },
            sheetState = sheetState,
        ) {
            @Suppress("DEPRECATION")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.salaryInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onSalaryChange(newValue)
                        }
                    },
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
private fun YearTotalCard(
    totalAmount: Double,
    moneyFormatter: CompactDecimalFormat
) {
    val amountColor = when {
        totalAmount > 0 -> Color(0xFF2E7D32)
        totalAmount < 0 -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sign = if (totalAmount > 0) "+" else if (totalAmount < 0) "-" else ""
            Text(
                text = "$sign${moneyFormatter.format(abs(totalAmount))}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
            )
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
    onExpandToggle: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onEditUserPayment: (UserPayment) -> Unit,
    onDeleteUserPayment: (UserPayment) -> Unit
) {
    val now = remember { LocalDate.now() }
    val isCurrentMonth = monthDate.monthValue == now.monthValue && monthDate.year == now.year
    val cardBg = if (isCurrentMonth) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = cardBg
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
                    val baseTitle = monthDate.format(monthNameFormatter).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                    }
                    if (monthDate.year > now.year) "$baseTitle ${monthDate.year}" else baseTitle
                }

                val isPastMonth = monthDate.isBefore(now.withDayOfMonth(1))
                val titleAlpha = if (isPastMonth) 0.6f else 1f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha),
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )

                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Меню",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Добавить выплату") },
                                onClick = {
                                    showMenu = false
                                    onAddPaymentClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isExpanded) "Скрыть календарь" else "Показать календарь") },
                                onClick = {
                                    showMenu = false
                                    onExpandToggle()
                                },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                            )
                        }
                    }
                }

                events.forEach { event ->
                    if (event.userPayment != null) {
                        InteractivePaymentRow(
                            event = event,
                            moneyFormatter = moneyFormatter,
                            dateFormatter = dateFormatter,
                            isPast = event.date.isBefore(now),
                            onDelete = { onDeleteUserPayment(event.userPayment) },
                            onEdit = { onEditUserPayment(event.userPayment) },
                            backgroundColor = cardBg
                        )
                    } else {
                        PaymentRow(
                            amount = event.amount,
                            moneyFormatter = moneyFormatter,
                            dateText = event.date.format(dateFormatter),
                            isPast = event.date.isBefore(now)
                        )
                    }
                }

                if (events.isNotEmpty()) {
                    val totalAmount = events.sumOf { it.amount }
                    val amountColor = when {
                        totalAmount > 0 -> Color(0xFF2E7D32)
                        totalAmount < 0 -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val sign = if (totalAmount > 0) "+" else if (totalAmount < 0) "-" else ""
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$sign${moneyFormatter.format(abs(totalAmount))}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = amountColor
                            )
                        )
                    }
                }

                if (isExpanded && monthMeta != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarGrid(monthMeta, locale)
                }
            }

            if (showDivider && !isCurrentMonth) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InteractivePaymentRow(
    event: PaymentEvent,
    moneyFormatter: CompactDecimalFormat,
    dateFormatter: DateTimeFormatter,
    isPast: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    backgroundColor: Color
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // Не удаляем строку при свайпе вправо
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isEdit = direction == SwipeToDismissBoxValue.StartToEnd
            val isDelete = direction == SwipeToDismissBoxValue.EndToStart
            
            val color = when {
                isEdit -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                isDelete -> Color.Red.copy(alpha = 0.8f)
                else -> Color.Transparent
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (dismissState.progress > 0) color else Color.Transparent)
                    .padding(horizontal = 16.dp),
                contentAlignment = if (isEdit) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.progress > 0.1f) {
                    Icon(
                        imageVector = if (isEdit) Icons.Default.Edit else Icons.Default.Delete,
                        contentDescription = if (isEdit) "Редактировать" else "Удалить",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        PaymentRow(
            amount = event.amount,
            moneyFormatter = moneyFormatter,
            dateText = event.date.format(dateFormatter),
            isPast = isPast,
            modifier = Modifier
                .background(backgroundColor)
                .padding(vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentDialog(
    initialDate: LocalDate,
    initialAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Double) -> Unit
) {
    var amountText by remember { mutableStateOf(initialAmount?.toString()?.replace(".0", "") ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAmount != null) "Редактировать выплату" else "Добавить выплату или расход") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // Разрешаем только цифры, одну точку или одну запятую, и минус в начале
                        val filtered = newValue.replace(',', '.')
                        if (filtered.isEmpty() || 
                            filtered == "-" || 
                            filtered.toDoubleOrNull() != null || 
                            (filtered.count { it == '.' } <= 1 && filtered.all { it.isDigit() || it == '.' || it == '-' })
                        ) {
                            amountText = newValue
                        }
                    },
                    label = { Text("Сумма (расход с минусом)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val amount = amountText.replace(',', '.').toDoubleOrNull()
                            if (amount != null) {
                                onConfirm(selectedDate, amount)
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    onValueChange = {},
                    label = { Text("Дата") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    onConfirm(selectedDate, amount)
                },
                enabled = amountText.isNotEmpty() && amountText != "-"
            ) {
                Text(if (initialAmount != null) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun CalendarGrid(monthMeta: MonthMeta, locale: Locale) {
    val yearMonth = YearMonth.of(monthMeta.year, monthMeta.monthNum)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(monthMeta.year, monthMeta.monthNum, 1)

    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val emptyCellsBefore = firstDayOfWeek - 1

    Column(modifier = Modifier.fillMaxWidth()) {
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

        val totalCells = daysInMonth + emptyCellsBefore
        val rows = (totalCells + 6) / 7

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val day = cellIndex - emptyCellsBefore + 1

                    if (cellIndex < emptyCellsBefore || day > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val isDayOff = monthMeta.isDayOff(day)
                        val isPreHoliday = monthMeta.isPreHoliday(day)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.6.sp
                                ),
                                color = when {
                                    isDayOff -> Color.Red
                                    isPreHoliday -> Color.Red.copy(alpha = 0.5f)
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
    amount: Double,
    moneyFormatter: CompactDecimalFormat,
    dateText: String,
    isPast: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val amountColor = when {
            amount > 0 -> Color(0xFF2E7D32)
            amount < 0 -> Color.Red
            else -> MaterialTheme.colorScheme.onSurface
        }
        val baseDateColor = MaterialTheme.colorScheme.onSurfaceVariant

        val color = if (isPast) amountColor.copy(alpha = 0.3f) else amountColor
        val dateColor = if (isPast) baseDateColor.copy(alpha = 0.3f) else baseDateColor

        val sign = if (amount > 0) "+" else if (amount < 0) "-" else ""

        Text(
            text = "$sign${moneyFormatter.format(abs(amount))}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
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
