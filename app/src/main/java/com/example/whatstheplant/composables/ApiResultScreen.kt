package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.plantid.model.Plant
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.lightBlue
import com.example.whatstheplant.ui.theme.lightGreen
import com.example.whatstheplant.ui.theme.yellowSun
import com.example.whatstheplant.viewModel.PlantViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import kotlinx.coroutines.launch
import kotlin.math.min

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ApiResultScreen(
    navController: NavController,
    apiResult: MutableState<Plant?>,
    plantViewModel: PlantViewModel,
    userData: UserData
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()

    val imageUrl = apiResult.value?.input?.images?.get(0)!!
    val username = userData.username
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


    val firstSuggestion = apiResult.value?.result?.classification?.suggestions?.get(0)
    val latitude = apiResult.value?.input?.latitude
    val longitude = apiResult.value?.input?.longitude

    val similarImages = firstSuggestion?.similar_images?.map { it.url } ?: emptyList()
    val imgs : List<String?> =listOf(imageUrl) + similarImages

    val name = firstSuggestion?.name
    val commonNames = firstSuggestion?.details?.common_names.toString()
    val description = firstSuggestion?.details?.description?.value

    val bestLightCondition = firstSuggestion?.details?.best_light_condition
    val bestSoilType = firstSuggestion?.details?.best_soil_type
    val bestWatering = firstSuggestion?.details?.best_watering
    //val watering = firstSuggestion?.details?.watering

    val taxonomy = firstSuggestion?.details?.taxonomy //TODO Make taxonomy appearance better
    //val synonyms = firstSuggestion?.details?.synonyms
    val edibleParts = firstSuggestion?.details?.edible_parts.toString()
    val commonUses = firstSuggestion?.details?.common_uses
    val culturalSignificance = firstSuggestion?.details?.cultural_significance
    val toxicity = firstSuggestion?.details?.toxicity
    val shownText =
        remember {
            mutableStateOf(taxonomy.toString())
        }
    val chosenButton = remember {
        mutableStateOf("Taxonomy")
    }
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        floatingActionButton = {
            Box(modifier = Modifier.padding(bottom = 80.dp)) {
                FloatingActionButton(
                    onClick = {
                        val firestorePlant = FirestorePlant( user_id = userData.userId, username = username!!, plant_id = "", similarImages = similarImages,
                            name = name, commonNames = commonNames, description = description, bestLightCondition = bestLightCondition,
                            bestSoilType = bestSoilType, bestWatering = bestWatering, taxonomy = taxonomy, edibleParts = edibleParts,
                            commonUses = commonUses, culturalSignificance = culturalSignificance, toxicity = toxicity,
                            img_url = imageUrl, latitude = latitude, longitude = longitude
                        )
                        plantViewModel.addPlant(firestorePlant)
                        navController.navigate(NavItem.Home.path)
                        Toast.makeText(
                            context,
                            "Plant added to your garden.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    containerColor = lightGreen,
                    content = {
                        Icon(Icons.Filled.Add, contentDescription = "Add to Garden", tint = Color.White)
                    })
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = {

            Column {
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
                    val pagerState = rememberPagerState(pageCount = { imgs.size})
                    HorizontalPager(
                        state = pagerState,
                        key = { imgs[it]!! },

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
                    state = scrollState, modifier = Modifier
                        .fillMaxSize()
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = description.toString(),
                                style = MaterialTheme.typography.bodyLarge,
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
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Text(
                                bestWatering.toString(),
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

                        HorizontalDivider(
                            thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                            )
                        )
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
                            if (edibleParts != "null") {
                                item {
                                    ElevatedButton(
                                        modifier = Modifier.padding(8.dp),
                                        colors = if (chosenButton.value == "Edible Parts") ButtonDefaults.buttonColors(
                                            lightGreen
                                        ) else ButtonDefaults.buttonColors(
                                            darkGreen
                                        ),
                                        onClick = {
                                            shownText.value = edibleParts
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
                                        Text(text = "Cultural Significance", color = Color.White)
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
                    if(latitude!= null && longitude != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, bottom = 32.dp, end = 16.dp)
                                    .height(300.dp)
                            ) {
                                MapboxMap(
                                    Modifier.fillMaxSize(),
                                    mapViewportState = rememberMapViewportState {
                                        setCameraOptions {
                                            zoom(11.0)
                                            center(Point.fromLngLat(longitude, latitude))
                                            pitch(0.0)
                                            bearing(0.0)
                                        }
                                    }
                                )
                                {
                                    val marker = rememberIconImage(
                                        key = R.drawable.red_marker,
                                        painter = painterResource(R.drawable.red_marker)
                                    )
                                    PointAnnotation(point = Point.fromLngLat(longitude, latitude)) {
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
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun NewPreview() {
    val scrollState = rememberLazyListState()
    val scrollOffset = remember {
        derivedStateOf {
            min(1f, 1 - (scrollState.firstVisibleItemScrollOffset / 600f))
        }
    }
    Scaffold(
        floatingActionButton = {
            Box {
                LargeFloatingActionButton(onClick = { /* Handle FAB click */ },
                    containerColor = Color(216, 245, 136),
                    content = {
                        Icon(Icons.Filled.Add, contentDescription = "Add to Garden")
                    })
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = {

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp * scrollOffset.value)
                        .graphicsLayer {
                            scaleX = scrollOffset.value
                            scaleY = scrollOffset.value
                        }
                ) {
                    val pics = listOf(

                        R.drawable.logo,
                        R.drawable.poppies,
                        R.drawable.sfondo
                    )
                    val pagerState = rememberPagerState(pageCount = { 3 })
                    HorizontalPager(
                        state = pagerState,
                        key = { pics[it] }
                    ) { index ->
                        Image(
                            painter = painterResource(id = pics[index]), contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                LazyColumn(
                    state = scrollState, modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 40.dp)
                ) {


                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Basilico del ",
                                style = MaterialTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .padding(start = 32.dp, end = 32.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Commonly Known As: Senegal Mahogany, African mahogany",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Khaya senegalensis is a species of tree in the Meliaceae family that is native to Africa. Common names include African mahogany, dry zone mahogany, Gambia mahogany, khaya wood, Senegal mahogany, cailcedrat, acajou, djalla, and bois rouge.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
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
                                    text = "Watering Needs",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Text(
                                "Watering this plant requires a balanced approach. It thrives with regular watering, especially during its early growth stages. However, it is crucial to avoid waterlogging, as this can harm the roots. During the dry season, increase the frequency of watering to ensure the soil remains moist but not soggy. Once established, it becomes more drought-tolerant and can survive with less frequent watering.",
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
                                    text = "Watering Needs",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Text(
                                "Watering this plant requires a balanced approach. It thrives with regular watering, especially during its early growth stages. However, it is crucial to avoid waterlogging, as this can harm the roots. During the dry season, increase the frequency of watering to ensure the soil remains moist but not soggy. Once established, it becomes more drought-tolerant and can survive with less frequent watering.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

