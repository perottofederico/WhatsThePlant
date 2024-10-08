package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.firestore.FirestoreTask
import com.example.whatstheplant.ui.theme.PurpleGrey40
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.lightBlue
import com.example.whatstheplant.ui.theme.lightGreen
import com.example.whatstheplant.ui.theme.veryLightGreen
import com.example.whatstheplant.ui.theme.yellowSun
import com.example.whatstheplant.viewModel.PlantViewModel
import com.example.whatstheplant.viewModel.TaskViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalMaterial3Api::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlantDetail(
    plantViewModel: PlantViewModel,
    taskViewModel: TaskViewModel,
    navController: NavController
) {
    val plant by plantViewModel.selectedPlant.observeAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var expanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()


    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    plant?.let {
        val scrollState = rememberLazyListState()
        val isEnlarged = remember {
            mutableStateOf(false)
        }
        val dynamicValue = remember {
            derivedStateOf {
                if (350.dp - Dp(scrollState.firstVisibleItemScrollOffset.toFloat()) < 108.dp || scrollState.firstVisibleItemIndex != 0) 108.dp //prevent going 0 cause crash
                else if (isEnlarged.value) {
                    600.dp - Dp(scrollState.firstVisibleItemScrollOffset.toFloat())
                } else
                    350.dp - Dp(scrollState.firstVisibleItemScrollOffset.toFloat())
            }
        }  //TODO make it so image stays on top of scrolling page ?
        val animateImageSize =
            animateDpAsState(dynamicValue.value, label = "imageSizeAnimatedValue").value


        val similarImages = plant?.similarImages ?: emptyList()
        val imgUrl = plant!!.img_url
        val imgs: List<String?> = listOf(imgUrl) + similarImages

        val name = plant?.name
        val commonNames = plant?.commonNames
        val description = plant?.description

        val bestLightCondition = plant?.bestLightCondition
        val bestSoilType = plant?.bestSoilType
        val bestWatering = plant?.bestWatering
        //val watering = plant?.watering

        val taxonomy = plant?.taxonomy //TODO Make taxonomy appearance better
        //val synonyms = plant?.synonyms
        val edibleParts = plant?.edibleParts
        val commonUses = plant?.commonUses
        val culturalSignificance = plant?.culturalSignificance
        val toxicity = plant?.toxicity

        val latitude = plant?.latitude
        val longitude = plant?.longitude

        val shownText =
            remember {
                mutableStateOf(taxonomy.toString())
            }
        val chosenButton = remember {
            mutableStateOf("Taxonomy")
        }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkGreen,
                        scrolledContainerColor = darkGreen,
                        titleContentColor = Color.White,
                    ),

                    title = { },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                            //navController.navigate("Home")
                        }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            expanded = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Schedule",
                                tint = Color.White
                            )
                            DropdownMenu(
                                modifier = Modifier.background(veryLightGreen),
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                plant?.let {
                                    DropDownMenuContent(
                                        plant = it,
                                        taskViewModel = taskViewModel,
                                        onConfirmClick = { expanded = false }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = {
                            plant?.let {
                                plantViewModel.deletePlant(
                                    userId = it.user_id,
                                    plantId = it.plant_id!!
                                )
                            }
                            navController.navigate("Home")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Plant",
                                tint = Color.White
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    // Image and Name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(animateImageSize)
                            .padding(top = 108.dp)
                            .clickable {
                                isEnlarged.value = !isEnlarged.value
                            }
                    ) {
                        val pagerState =
                            rememberPagerState(pageCount = { imgs.size })
                        HorizontalPager(
                            state = pagerState,
                            key = { imgs[it]!! }
                        ) { index ->
                            AsyncImage(
                                model = imgs[index],
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            Modifier
                                .wrapContentHeight()
                                .align(Alignment.BottomCenter)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                val color =
                                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(12.dp)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = name.toString(),
                            style = typography.displayMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)  // This will make the LazyColumn take up the remaining space
                            .padding(bottom = 90.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {


                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                                    if (commonNames != null) {
                                        Text(
                                            text = "Commonly Known As: ${
                                                commonNames.substring(
                                                    1,
                                                    commonNames.length - 1
                                                )
                                            }",
                                            style = typography.titleMedium,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = description.toString(),
                                    style = typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }


                        // Watering
                        item {
                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.WaterDrop,
                                        contentDescription = "User Icon",
                                        tint = lightBlue
                                    )
                                    Text(
                                        text = "Watering Guide",
                                        style = typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    bestWatering.toString(),
                                    style = typography.bodyLarge,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        item {

                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )
                        }


                        // Sunlight
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.WbSunny,
                                        contentDescription = "User Icon",
                                        tint = yellowSun
                                    )
                                    Text(
                                        text = "Sunlight Tips",
                                        style = typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    bestLightCondition.toString(),
                                    style = typography.bodyLarge,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        item {

                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )
                        }


                        // Soil type
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Grass,
                                        contentDescription = "User Icon",
                                        tint = darkGreen
                                    )
                                    Text(
                                        text = "Soil Suggestions",
                                        style = typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    text = bestSoilType.toString(),
                                    style = typography.bodyLarge,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        item {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                if (taxonomy.toString() != "null") {
                                    item {
                                        ElevatedButton(
                                            modifier = Modifier.padding(8.dp),
                                            colors = if (chosenButton.value == "Taxonomy") ButtonDefaults.buttonColors(
                                                lightGreen
                                            ) else ButtonDefaults.buttonColors(
                                                darkGreen
                                            ),
                                            onClick = {
                                                shownText.value = taxonomy.toString()
                                                chosenButton.value = "Taxonomy"
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(index = 10)
                                                }
                                            }
                                        ) {
                                            Text(text = "Taxonomy", color = Color.White)
                                        }
                                    }
                                }
                                if (edibleParts.toString() != "null") {
                                    item {
                                        ElevatedButton(
                                            modifier = Modifier.padding(8.dp),
                                            colors = if (chosenButton.value == "Edible Parts") ButtonDefaults.buttonColors(
                                                lightGreen
                                            ) else ButtonDefaults.buttonColors(
                                                darkGreen
                                            ),
                                            onClick = {
                                                shownText.value = edibleParts.toString()
                                                chosenButton.value = "Edible Parts"
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(index = 10)
                                                }
                                            }
                                        ) {
                                            Text(text = "Edible Parts", color = Color.White)
                                        }
                                    }
                                }
                                if (toxicity.toString() != "null") {
                                    item {
                                        ElevatedButton(
                                            modifier = Modifier.padding(8.dp),
                                            colors = if (chosenButton.value == "Toxicity") ButtonDefaults.buttonColors(
                                                lightGreen
                                            ) else ButtonDefaults.buttonColors(
                                                darkGreen
                                            ),
                                            onClick = {
                                                shownText.value = toxicity.toString()
                                                chosenButton.value = "Toxicity"
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(index = 10)
                                                }
                                            }
                                        ) {
                                            Text(text = "Toxicity", color = Color.White)
                                        }
                                    }
                                }
                                if (commonUses.toString() != "null") {
                                    item {
                                        ElevatedButton(
                                            modifier = Modifier.padding(8.dp),
                                            colors = if (chosenButton.value == "Common Uses") ButtonDefaults.buttonColors(
                                                lightGreen
                                            ) else ButtonDefaults.buttonColors(
                                                darkGreen
                                            ),
                                            onClick = {
                                                shownText.value = commonUses.toString()
                                                chosenButton.value = "Common Uses"
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(index = 10)
                                                }
                                            }
                                        ) {
                                            Text(text = "Common Uses", color = Color.White)
                                        }
                                    }
                                }

                                if (culturalSignificance.toString() != "null") {
                                    item {
                                        ElevatedButton(
                                            modifier = Modifier.padding(8.dp),
                                            colors = if (chosenButton.value == "Cultural Significance") ButtonDefaults.buttonColors(
                                                lightGreen
                                            ) else ButtonDefaults.buttonColors(
                                                darkGreen
                                            ),
                                            onClick = {
                                                shownText.value = culturalSignificance.toString()
                                                chosenButton.value = "Cultural Significance"
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(index = 10)
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = "Cultural Significance",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                            }
                        }

                        item {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = chosenButton.value,
                                        style = typography.headlineMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                if (chosenButton.value == "Taxonomy") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column {
                                            Text(text = "Kingdom: ${taxonomy?.kingdom}")
                                            Text(text = "Class: ${taxonomy?.`class`}")
                                            Text(text = "Family: ${taxonomy?.family}")
                                        }
                                        Column {
                                            Text(text = "Phylum: ${taxonomy?.phylum}")
                                            Text(text = "Order: ${taxonomy?.order}")
                                            Text(text = "Genus: ${taxonomy?.genus}")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = shownText.value,
                                        modifier = Modifier.padding(
                                            bottom = 32.dp,
                                            top = 8.dp,
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )
                        }
                        if (latitude != null && longitude != null) {
                            item {
                                val mapState = rememberMapState {
                                    gesturesSettings = GesturesSettings {
                                        pitchEnabled = false
                                        scrollEnabled = true
                                    }
                                }
                                val viewport = rememberMapViewportState {
                                    setCameraOptions {
                                        zoom(11.0)
                                        center(
                                            Point.fromLngLat(
                                                plant!!.longitude!!.toDouble(),
                                                plant!!.latitude!!.toDouble()
                                            )
                                        )
                                        pitch(0.0)
                                        bearing(0.0)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, bottom = 32.dp, end = 16.dp)
                                        .height(300.dp)
                                ) {
                                    MapboxMap(
                                        Modifier
                                            .fillMaxSize()
                                            .height(300.dp),
                                        mapViewportState = viewport,
                                        mapState = mapState
                                    ) {
                                        val marker = rememberIconImage(
                                            key = R.drawable.red_marker,
                                            painter = painterResource(R.drawable.red_marker)
                                        )
                                        PointAnnotation(
                                            point = Point.fromLngLat(
                                                longitude,
                                                latitude
                                            )
                                        ) {
                                            // specify the marker image
                                            iconImage = marker
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (showDatePicker) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = {},
                            trailingIcon = {
                                IconButton(onClick = { showCalendarPicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "select"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp)
                                .height(64.dp)
                        )
                    }

                    if (showCalendarPicker) {
                        Popup(
                            onDismissRequest = { showDatePicker = false },
                            alignment = Alignment.TopStart,
                            offset = IntOffset(0, 300)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = 64.dp)
                                    .shadow(elevation = 4.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    showModeToggle = false
                                )
                            }
                        }
                    }
                }
            }
        )
    } ?: run {
        // Shouldn't be possible but you never know
        Text(text = "No plant selected", style = typography.bodyMedium)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuContent(
    plant: FirestorePlant,
    taskViewModel: TaskViewModel,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current

    //First field - Task type
    val tasks = listOf("Water", "Fertilize", "Prune")
    var chosenTask by remember {
        mutableStateOf("Choose Task Type")
    }
    var expandTasksMenu by remember {
        mutableStateOf(false)
    }

    //Second Field - Start Date
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    val startDatePickerState = rememberDatePickerState()
    var showStartPopup by remember {
        mutableStateOf(false)
    }

    //Third Field - End Date
    var endDate by remember { mutableStateOf("Select Date") }
    val endDatePickerState = rememberDatePickerState()
    var showEndPopup by remember {
        mutableStateOf(false)
    }

    //Last Field - Frequency
    var freqStr by remember { mutableStateOf("") }
    var freq by remember {
        mutableIntStateOf(1)
    }

    // First Field - Task Type
    DropdownMenuItem(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(width = 1.dp, color = PurpleGrey40, shape = RoundedCornerShape(4.dp)),
        text = { Text(chosenTask) },
        onClick = { expandTasksMenu = !expandTasksMenu },
        trailingIcon = {
            if (expandTasksMenu) {
                Icon(
                    Icons.Outlined.ArrowDropUp,
                    contentDescription = null
                )
            } else {
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null
                )
            }
        }
    )
    if (expandTasksMenu) {
        DropdownMenu(
            modifier = Modifier.background(veryLightGreen)
                .border(width = 1.dp, color = PurpleGrey40, shape = RectangleShape),
            expanded = expandTasksMenu,
            onDismissRequest = { expandTasksMenu = false },
            offset = DpOffset(200.dp, (-162).dp)
        ) {
            tasks.forEachIndexed { index, task ->
                DropdownMenuItem(text = {
                    Text(text = task, textAlign = TextAlign.Center)
                },
                    onClick = {
                        chosenTask = task
                        expandTasksMenu = false
                    }
                )
                if(index<2) HorizontalDivider(color = PurpleGrey40)
            }
        }
    }


    //Second Field - Start Date
    DropdownMenuItem(onClick = { showStartPopup = true },
        text = {
            OutlinedTextField(
                value = startDate.replace("-", "/"),
                readOnly = true,
                onValueChange = { startDate = it },
                label = { Text("Start Date") },
                trailingIcon = {
                    IconButton(onClick = { showStartPopup = !showStartPopup }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                    }
                }
            )
        }
    )
    // Popup with Date Picker
    if (showStartPopup) {
        DatePickerDialog(
            onDismissRequest = { showStartPopup = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = (convertMillisToDate(startDatePickerState.selectedDateMillis!!))
                    showStartPopup = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPopup = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = startDatePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = veryLightGreen)
            )
        }
    }

    //Third Field - End Date
    DropdownMenuItem(onClick = { showEndPopup = true },
        text = {
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date") },
                trailingIcon = {
                    IconButton(onClick = { showEndPopup = !showEndPopup }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                    }
                }
            )
        }
    )
    // Popup with Date Picker
    if (showEndPopup) {
        DatePickerDialog(
            onDismissRequest = { showEndPopup = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = (convertMillisToDate(endDatePickerState.selectedDateMillis!!))
                    if (endDate < startDate) startDate = endDate
                    showEndPopup = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPopup = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = endDatePickerState,
                showModeToggle = false
            )
        }
    }

    //Last Field - Frequency
    DropdownMenuItem(onClick = { },
        text = {
            OutlinedTextField(
                value = freqStr,
                onValueChange = {
                    if (it == "") {
                        freqStr = ""
                    }
                    if (it.toIntOrNull() != null && it.toInt() > 0) {
                        freq = it.toInt()
                        freqStr = it
                    }
                },
                label = { Text("Frequency (Days)") },
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Frequency")
                    }
                }
            )
        }
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
    DropdownMenuItem(
        onClick = {},
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = {
                    if (chosenTask == "Choose Task Type" || endDate == "Select Date" || freq == 0 || endDate == "") {
                        Toast.makeText(
                            context,
                            "All fields must have a value.",
                            LENGTH_SHORT
                        ).show()
                    } else {
                        //Create task object
                        val task = plant.plant_id?.let {
                            FirestoreTask(
                                userId = plant.user_id,
                                plantId = it,
                                plantName = plant.name!!,
                                taskId = "",
                                type = chosenTask,
                                startDate = startDate.replace("/", "-"),
                                endDate = endDate.replace("/", "-"),
                                frequency = freq
                            )
                        }
                        if (task != null) {
                            taskViewModel.addTask(task)
                            expandTasksMenu = false
                            showStartPopup = false
                            showEndPopup = false
                            onConfirmClick()
                        }
                        Toast.makeText(
                            context,
                            "Task schedule added.",
                            LENGTH_LONG
                        ).show()
                    }
                }) {
                    Text("Confirm", color = Color.Black)
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }
    )
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return formatter.format(Date(millis))
}


@Preview(showSystemUi = true)
@Composable
fun Preview() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Taxonomy",
                style = typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                val str1 = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("Kingdom: ")
                    }
                    append("Plantae")
                }
                Text(text = str1)
                Text(text = "Phylum: Tracheophyta")
                Text(text = "Class: Magnoliiopsida")
            }
            Column {
                Text(text = "Order: Lamiales")
                Text(text = "Family: Scrophulariaceae")
                Text(text = "Genus: Buddleja")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TaskPreview() {
    // First Field - Task Type

    DropdownMenuItem(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(width = 1.dp, color = PurpleGrey40, shape = RoundedCornerShape(4.dp)),
        text = { Text("ASEF") },
        onClick = { },
        trailingIcon = {
            if (true) {
                Icon(
                    Icons.Outlined.ArrowDropUp,
                    contentDescription = null
                )
            } else {
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null
                )
            }
        }
    )
    if (true) {
        DropdownMenu(
            modifier = Modifier.background(veryLightGreen),
            expanded = true,
            onDismissRequest = { },
            offset = DpOffset(200.dp, 200.dp)
        ) {
            DropdownMenuItem(text = {
                Text(text = "task")
            },
                onClick = {
                }
            )
        }
    }
}