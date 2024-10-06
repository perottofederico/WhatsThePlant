package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.plantid.model.Taxonomy
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.lightBlue
import com.example.whatstheplant.ui.theme.lightGreen
import com.example.whatstheplant.ui.theme.yellowSun
import com.example.whatstheplant.viewModel.PlantViewModel
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.animation.moveBy
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlantDetail(plantViewModel: PlantViewModel, navController: NavController) {
    val plant by plantViewModel.selectedPlant.observeAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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

        val latitude = plant?.latitude?.toDouble()
        val longitude = plant?.longitude?.toDouble()

        val shownText =
            remember {
                mutableStateOf(taxonomy.toString())
            }
        val chosenButton = remember {
            mutableStateOf("Taxonomy")
        }
        val coroutineScope = rememberCoroutineScope()
        var isMapTouched by remember { mutableStateOf(false) }

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
                                contentDescription = "Localized description",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            plant?.let { plantViewModel.deletePlant(userId = it.user_id,  plantId = it.plant_id!!) }
                            navController.navigate("Home")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description",
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
                            rememberPagerState(pageCount = { imgs.size ?: 0 })
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
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    /*
                    Column(
                        modifier = Modifier.verticalScroll(columnScrollState)
                            .fillMaxSize()
                            .weight(1f)  // This will make the LazyColumn take up the remaining space
                            .padding(bottom = 90.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {


                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                                if (commonNames != null) {
                                    Text(
                                        text = "Commonly Known As: ${ // TODO make it bold?
                                            commonNames.substring(
                                                1,
                                                commonNames.length - 1
                                            )
                                        }",
                                        style = MaterialTheme.typography.titleMedium,
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

                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )

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

                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )

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
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    bestLightCondition.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
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
                                            longitude!!,
                                            latitude!!
                                        )
                                    ) {
                                        // specify the marker image
                                        iconImage = marker
                                    }
                                }
                            }
                        }
                    }
                    */

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)  // This will make the LazyColumn take up the remaining space
                            .padding(bottom = 90.dp)
                            .pointerInteropFilter { event ->

                                // Only allow scrolling if the map is not touched
                                if (isMapTouched) {
                                    true // Prevent LazyColumn from scrolling when the map is touched
                                } else {
                                    false // Allow LazyColumn to handle scroll events when the map is not touched
                                }
                            }
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
                                            text = "Commonly Known As: ${ // TODO make it bold?
                                                commonNames.substring(
                                                    1,
                                                    commonNames.length - 1
                                                )
                                            }",
                                            style = MaterialTheme.typography.titleMedium,
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
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    bestLightCondition.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
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
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    text = bestSoilType.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
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
                                        style = MaterialTheme.typography.headlineMedium,
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
                                var mapInteractionEnabled by remember { mutableStateOf(false) }

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
                                    /*
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            Log.d(
                                                "DRAG",
                                                "Ddrag on MapboxMap: $dragAmount"
                                            )
                                            coroutineScope.launch {
                                                val cameraState = viewport.cameraState
                                                if (cameraState != null) {
                                                    val currentCenter = cameraState.center
                                                    val newLatitude = currentCenter.latitude() + (dragAmount.y / 300)
                                                    val newLongitude = currentCenter.longitude() - (dragAmount.x / 300)
                                                    viewport.moveBy(screenCoordinate = ScreenCoordinate(dragAmount.x.toDouble(), dragAmount.y.toDouble()))
                                                }
                                            }
                                        }
                                    }
                                     */
                                ) {
                                    MapboxMap(
                                        Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    Log.d(
                                                        "DRAG",
                                                        "Ddrag on MapboxMap: $dragAmount"
                                                    )
                                                    coroutineScope.launch {
                                                        val cameraState = viewport.cameraState
                                                        if (cameraState != null) {
                                                            val currentCenter = cameraState.center
                                                            val newLatitude =
                                                                currentCenter.latitude() + (dragAmount.y / 300)
                                                            val newLongitude =
                                                                currentCenter.longitude() - (dragAmount.x / 300)
                                                            viewport.easeTo(
                                                                cameraOptions = cameraOptions {
                                                                    center(
                                                                        Point.fromLngLat(
                                                                            newLongitude,
                                                                            newLatitude
                                                                        )
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
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
            }
        )
    } ?: run {
        // Shouldn't be possible but you never know
        Text(text = "No plant selected", style = typography.bodyMedium)
    }
}

@Preview(showSystemUi = true)
@Composable
fun preview() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Taxonomy",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column() {
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
            Column() {
                Text(text = "Order: Lamiales")
                Text(text = "Family: Scrophulariaceae")
                Text(text = "Genus: Buddleja")
            }
        }
    }
}