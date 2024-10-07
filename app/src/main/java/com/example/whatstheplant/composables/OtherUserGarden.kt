package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.veryLightGreen
import com.example.whatstheplant.viewModel.PlantViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserGarden(
    navController: NavController,
    plantViewModel: PlantViewModel,
    userId: String
){

    // Use LaunchedEffect to trigger the API call only once when the HomeScreen is first composed
    LaunchedEffect(Unit) {
        plantViewModel.fetchPlantList(userId = userId)
    }

    val plantsList = plantViewModel.plantsList

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${plantsList?.get(0)?.username}'s Garden",
                            style = typography.headlineMedium
                        )
                    }
                }
            )
        }
    ){
        if (plantsList == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .size(60.dp),
                    color = darkGreen,
                    trackColor = Color.White
                )
            }
        } else if (plantsList.isEmpty()) {
            // Show a message when there are no plants
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No plants found. Add a plant to get started.",
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 108.dp,
                        bottom = 120.dp
                    )
            ) {
                items(plantsList.size) { index ->
                    val plant = plantsList[index]
                    PlantItem(plant = plant, onClick = {
                        // Navigate to the details page of the plant when clicked
                        plantViewModel.setSelectedPlant(plant)
                        navController.navigate("PlantDetail")
                    })
                }
            }
        }
    }
}


// Composable for each plant item in the grid
@Composable
fun PlantItem(plant: FirestorePlant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(containerColor = veryLightGreen),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(200.dp)
            //.wrapContentHeight()
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            //modifier = Modifier.height(250.dp)
        ) {

            AsyncImage(
                model = plant.img_url,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Text(
                text = plant.name!!,
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}