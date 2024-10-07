package com.example.whatstheplant.composables.tabs.calendar

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whatstheplant.api.firestore.FirestoreTask
import com.example.whatstheplant.composables.rememberFirstMostVisibleMonth
import com.example.whatstheplant.viewModel.TaskViewModel
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

/**
 * Composable function that represents the search screen of the application.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    userId : String,
    taskViewModel: TaskViewModel
) {

    LaunchedEffect(Unit) {
        taskViewModel.fetchTaskList(userId = userId)
    }
    val tasksList = taskViewModel.tasksList
    
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid,
    )
    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)

    // Precompute task dates
    val taskDatesMap = remember(tasksList) {
        tasksList?.flatMap { task ->
            generateTaskDates(task).map { date -> date to task }
        }?.groupBy({ it.first }, { it.second })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
            .background(Color.White),
    ) {

        SimpleCalendarTitle(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            currentMonth = visibleMonth.yearMonth,
            goToPrevious = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                }
            },
            goToNext = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                }
            },
        )

        HorizontalCalendar(
            modifier = Modifier.wrapContentWidth(),
            state = state,
            monthHeader = {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            },
            dayContent = {day ->
                val hasTask = taskDatesMap?.containsKey(day.date) ?: false
                val taskTypes = (taskDatesMap?.get(day.date))?.map {
                    it.type
                }?.toSet()

                Day(
                    day = day,
                    isSelected = selectedDate == day.date,
                    onClick = {
                        selectedDate = if (selectedDate == day.date) null else day.date
                    },
                    hasTask = hasTask,
                    taskTypes = taskTypes
                    )
            },
        )
        Text(text = taskDatesMap.toString())
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

// Function to generate all the task dates from startDate to endDate based on frequency
@RequiresApi(Build.VERSION_CODES.O)
fun generateTaskDates(task: FirestoreTask): List<LocalDate> {
    // cast to Local Date
    var formattedStartDate = LocalDate.parse(task.startDate)
    val formattedEndDate = LocalDate.parse(task.endDate)

    val taskDates = mutableListOf<LocalDate>()

    while (formattedStartDate <= formattedEndDate) {
        taskDates.add(formattedStartDate)
        formattedStartDate = formattedStartDate.plusDays(task.frequency.toLong())
    }
    return taskDates
}