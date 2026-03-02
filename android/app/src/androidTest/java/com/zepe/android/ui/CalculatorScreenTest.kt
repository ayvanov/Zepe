package com.zepe.android.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.zepe.android.MainActivity
import com.zepe.android.domain.model.MonthMeta
import org.junit.Rule
import org.junit.Test

class CalculatorScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rendersMonthCardsWhenStateHasData() {
        val state = CalculatorUiState(
            salaryInput = "100000",
            yearInput = "2026",
            months = listOf(
                MonthMeta(
                    monthSlice = "0000000000000001111111111111111",
                    nextMonthSlice = "1111000011110000111100001111000",
                    monthNum = 1,
                    salary = 100000,
                    year = 2026,
                )
            )
        )

        composeRule.setContent {
            CalculatorScreen(
                state = state,
                onSalaryChange = {},
                onYearChange = {},
                onCalculate = {},
            )
        }

        composeRule.onNodeWithText("Январь").assertIsDisplayed()
        composeRule.onNodeWithText("Аванс", substring = true).assertIsDisplayed()
    }
}
