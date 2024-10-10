package com.example.whatstheplant.composables.tabs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.veryLightGreen
import com.example.whatstheplant.viewModel.PlantViewModel
import com.example.whatstheplant.viewModel.UserViewModel
import kotlinx.coroutines.launch

enum class SocialPage(
    val title: String
    //,
    //@DrawableRes val drawableResId: Int
) {
    ALL("All"),// R.drawable.facebook_svg),
    FOLLOWED("Followed")//, R.drawable.google_svg)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    navController: NavController,
    userData: UserData,
    plantViewModel: PlantViewModel,
    pages: Array<SocialPage> = SocialPage.entries.toTypedArray(),
    userViewModel: UserViewModel
) {
    LaunchedEffect(Unit) {
        plantViewModel.fetchAllPlants()
        userViewModel.getUser(userData.userId)
    }

    val coroutineScope = rememberCoroutineScope()

    val plantsList = plantViewModel.allPlants?.filter { it.user_id != userData.userId }
    val followed = userViewModel.user?.followedList

    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = darkGreen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = darkGreen
                )
            }
        ) {
            pages.forEachIndexed { index, page ->
                val title = page.title
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = title) },
                    unselectedContentColor = Color.Gray,
                    selectedContentColor = Color.Black
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            when (pages[index]) {
                SocialPage.ALL -> AllFeed(
                    plantsList = plantsList,
                    userViewModel = userViewModel,
                    plantViewModel = plantViewModel,
                    navController = navController
                )

                SocialPage.FOLLOWED -> FollowedFeed(
                    plantsList = plantsList,
                    followed = followed,
                    plantViewModel = plantViewModel,
                    userViewModel = userViewModel,
                    navController = navController
                )
            }
        }

        HorizontalPager(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            state = pagerState,
            //verticalAlignment = Alignment.Top
        ) { index ->
            when (pages[index]) {
                SocialPage.ALL -> {
                    AllFeed(
                        plantsList = plantsList,
                        userViewModel = userViewModel,
                        plantViewModel = plantViewModel,
                        navController = navController
                    )
                }

                SocialPage.FOLLOWED -> {
                    FollowedFeed(
                        plantsList = plantsList,
                        followed = followed,
                        plantViewModel = plantViewModel,
                        userViewModel = userViewModel,
                        navController = navController
                    )
                }
            }
        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerScreen(
    pagerState: PagerState,
    pages: Array<SocialPage>,
    plantsList: List<FirestorePlant>?,
    followed: List<String>?,
    plantViewModel: PlantViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    Column(modifier = Modifier.padding(top = 60.dp)) {
        val coroutineScope = rememberCoroutineScope()
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = darkGreen,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = darkGreen
                )
            }
        ) {
            pages.forEachIndexed { index, page ->
                val title = page.title
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = title) },
                    unselectedContentColor = Color.Gray,
                    selectedContentColor = Color.Black
                )
            }
        }

        HorizontalPager(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            when (pages[index]) {
                SocialPage.ALL -> {
                    AllFeed(
                        plantsList = plantsList,
                        userViewModel = userViewModel,
                        plantViewModel = plantViewModel,
                        navController = navController
                    )
                }

                SocialPage.FOLLOWED -> {
                    FollowedFeed(
                        plantsList = plantsList,
                        followed = followed,
                        plantViewModel = plantViewModel,
                        userViewModel = userViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun FollowedFeed(
    plantsList: List<FirestorePlant>?,
    followed: List<String>?,
    plantViewModel: PlantViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {

    val followedUsersPlants = mutableListOf<FirestorePlant>() //emptyList<FirestorePlant>()
    plantsList?.forEach {
        if (followed != null) {
            if (followed.contains(it.user_id)) {
                followedUsersPlants.add(it)
            }
        }
    }
    Log.d("FilteredPlantList", followedUsersPlants.toString())

    if (followedUsersPlants.isNotEmpty()) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom=100.dp)
        ) {
            items(followedUsersPlants) { plant ->
                GardenListItem(
                    plant = plant,
                    onClick = {
                        plantViewModel.setSelectedPlant(plant)
                        navController.navigate("OtherUserPlantDetail")
                    },
                    userViewModel = userViewModel,
                    navController = navController
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "You aren't currently following anyone",
                style = typography.titleLarge
            )
        }
    }
}

@Composable
fun AllFeed(
    plantsList: List<FirestorePlant>?,
    plantViewModel: PlantViewModel,
    userViewModel: UserViewModel,
    navController: NavController,
) {

    var searchQuery by remember { mutableStateOf("") }
    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
    ) {
        // Filter the list based on search query
        val userList = plantsList?.map { it.username }?.distinct()
        val filteredUsers = userList?.filter { it.contains(searchQuery, ignoreCase = true) }
        if (plantsList != null) {
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Search for users...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedIndicatorColor = darkGreen,
                        focusedIndicatorColor = darkGreen,
                        focusedContainerColor = darkGreen.copy(alpha = 0.2f),
                        unfocusedContainerColor = darkGreen.copy(alpha = 0.1f)
                    )
                )
            }
            items(plantsList) { plant ->
                if (filteredUsers?.contains(plant.username) == true) {
                    GardenListItem(
                        plant = plant,
                        onClick = {
                            plantViewModel.setSelectedPlant(plant)
                            navController.navigate("OtherUserPlantDetail")
                        },
                        userViewModel = userViewModel,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        if (filteredUsers != null) {
            if (filteredUsers.isEmpty()){
                item{
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found")
                    }
                }
            }
        }
    }
}


@Composable
fun GardenListItem(
    plant: FirestorePlant,
    onClick: () -> Unit,
    userViewModel: UserViewModel,
    navController: NavController
) {
    Card(
        colors = CardDefaults.cardColors(veryLightGreen),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .height(160.dp),
        //elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {

            //Image
            Column(
                modifier = Modifier.padding(end = 16.dp, top = 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.Transparent
                ) {
                    AsyncImage(
                        model = plant.img_url,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                // first Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plant.username,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(darkGreen)
                    ) {
                        IconButton(
                            onClick = {
                                userViewModel.addFollow(
                                    userId = userViewModel.user!!.userId,
                                    plant.user_id
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (userViewModel.user?.followedList?.contains(plant.user_id) == false) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Follow",
                                    tint = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Unfollow",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(140.dp)
                    ) {
                        Text(
                            text = plant.name!!,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(darkGreen)
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("OtherUserGarden/${plant.user_id}")
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Yard,
                                contentDescription = "Open Garden",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun CardPreview() {
    Card(
        colors = CardDefaults.cardColors(veryLightGreen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(160.dp),
        //elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {

            //Image
            Column(
                modifier = Modifier.padding(end = 16.dp, top = 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.Transparent
                ) {
                    AsyncImage(
                        model = R.drawable.poppies,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            // Plant And User and Description
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MattyMatto",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(darkGreen)
                    ) {
                        IconButton(
                            onClick = {
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Follow",
                                tint = Color.White
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(140.dp)
                    ) {
                        Text(
                            text = "Ficus Benjamina",
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(darkGreen)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Yard,
                                contentDescription = "Follow",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
