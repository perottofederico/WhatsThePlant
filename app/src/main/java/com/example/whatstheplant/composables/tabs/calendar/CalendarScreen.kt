package com.example.whatstheplant.composables.tabs.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whatstheplant.api.firestore.FirestoreTask
import com.example.whatstheplant.composables.rememberFirstMostVisibleMonth
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.lightBlue
import com.example.whatstheplant.viewModel.TaskViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
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
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
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
                val taskTypes = (taskDatesMap?.get(day.date))?.map {
                    it.type
                }?.toSet()

                Day(
                    day = day,
                    isSelected = selectedDate == day.date,
                    onClick = {
                        selectedDate = if (selectedDate == day.date) null else day.date
                    },
                    taskTypes = taskTypes
                    )
            },
        )
        // Task info for selected date
        selectedDate?.let { date ->
            val tasksForSelectedDate = tasksList?.filter { task ->
                generateTaskDates(task).contains(date)
            }

            if (!tasksForSelectedDate.isNullOrEmpty()) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    ) {
                    tasksForSelectedDate.forEach { task ->

                        HorizontalDivider()
                        TaskRow(task = task)
                    }
                }
            } else {

                HorizontalDivider()
                // If no tasks for selected date
                Text(
                    text = "No tasks scheduled",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TaskRow(task: FirestoreTask) {
    val colorMap = mapOf("Watering" to lightBlue, "Pruning" to darkGreen,"Soil" to Color.Magenta)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Task type box
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(colorMap[task.type]!!)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Task details
        Column {
            Text(
                text = task.type,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = task.plantName,
                style = MaterialTheme.typography.bodySmall
            )
        }
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