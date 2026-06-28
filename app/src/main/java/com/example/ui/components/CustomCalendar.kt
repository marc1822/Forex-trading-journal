package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradeEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomCalendar(
    tradeEntries: List<TradeEntry>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonthYearString = remember(calendar) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(calendar.time)
    }

    // Days list for the current month
    val daysInMonth = remember(calendar) {
        getDaysInMonthList(calendar)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_calendar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Month Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            time = calendar.time
                            add(Calendar.MONTH, -1)
                        }
                        calendar = newCal
                    },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = currentMonthYearString,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            time = calendar.time
                            add(Calendar.MONTH, 1)
                        }
                        calendar = newCal
                    },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of week row
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(daysInMonth.size) { index ->
                    val dayInfo = daysInMonth[index]
                    if (dayInfo == null) {
                        Spacer(modifier = Modifier.fillMaxSize())
                    } else {
                        val dateString = dayInfo.dateString
                        val isSelected = dateString == selectedDate
                        val dayEntries = tradeEntries.filter { it.date == dateString }
                        val netProfit = dayEntries.sumOf { it.profitOrLoss }

                        // Check if color customization exists for entries in this day
                        val customColorEntry = dayEntries.firstOrNull { it.isCustomColor && !it.customColorHex.isNullOrBlank() }
                        val dayColor = when {
                            customColorEntry != null -> {
                                try {
                                    Color(android.graphics.Color.parseColor(customColorEntry.customColorHex))
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            else -> null
                        }

                        DayItem(
                            dayNumber = dayInfo.dayNumber,
                            netProfit = netProfit,
                            hasTrades = dayEntries.isNotEmpty(),
                            isSelected = isSelected,
                            customColor = dayColor,
                            onClick = { onDateSelected(dateString) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(
    dayNumber: Int,
    netProfit: Double,
    hasTrades: Boolean,
    isSelected: Boolean,
    customColor: Color?,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundThemeColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundThemeColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (hasTrades) {
                val formattedAmount = if (netProfit >= 0) {
                    "+$${netProfit.toInt()}"
                } else {
                    "-$${Math.abs(netProfit).toInt()}"
                }

                val textColor = when {
                    customColor != null -> customColor
                    netProfit > 0 -> Color(0xFF2E7D32) // Win Green
                    netProfit < 0 -> Color(0xFFC62828) // Loss Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

data class DayInfo(val dayNumber: Int, val dateString: String)

fun getDaysInMonthList(calendar: Calendar): List<DayInfo?> {
    val list = mutableListOf<DayInfo?>()
    val tempCal = Calendar.getInstance().apply {
        time = calendar.time
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val totalDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Fill offset with nulls
    for (i in 0 until firstDayOfWeek) {
        list.add(null)
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (day in 1..totalDays) {
        tempCal.set(Calendar.DAY_OF_MONTH, day)
        val dateString = sdf.format(tempCal.time)
        list.add(DayInfo(dayNumber = day, dateString = dateString))
    }

    return list
}
