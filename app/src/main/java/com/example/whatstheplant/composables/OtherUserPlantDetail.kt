package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.lightBlue
import com.example.whatstheplant.ui.theme.lightGreen
import com.example.whatstheplant.ui.theme.yellowSun
import com.example.whatstheplant.viewModel.PlantViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OtherUserPlantDetail(
    plantViewModel: PlantViewModel,
    navController: NavController
){
    val plant by plantViewModel.selectedPlant.observeAsState()
    plant?.let {
        val scrollState = rememberLazyListState()
        val isEnlarged = remember {
            mutableStateOf(false)
        }
        val dynamicValue = remember {
            derivedStateOf {
                if (350.dp - Dp(scrollState.firstVisibleItemScrollOffset.toFloat()) < 44.dp || scrollState.firstVisibleItemIndex != 0) 44.dp //prevent going 0 cause crash
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

        val shownText =
            remember {
                mutableStateOf(taxonomy.toString())
            }
        val chosenButton = remember {
            mutableStateOf("Taxonomy")
        }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
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
                            .padding(top = 40.dp)
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
                            style = MaterialTheme.typography.displayMedium,
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

                        item {
                            HorizontalDivider(
                                thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                )
                            )
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