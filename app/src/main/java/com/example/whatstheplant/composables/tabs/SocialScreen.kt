package com.example.whatstheplant.composables.tabs

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.plantid.model.Plant
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.veryLightGreen
import com.example.whatstheplant.viewModel.PlantViewModel
import com.example.whatstheplant.viewModel.UserViewModel
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SocialScreen(
    navController: NavController,
    userData: UserData,
    plantViewModel: PlantViewModel,
    pages: Array<SocialPage> = SocialPage.values(),
    userViewModel: UserViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    // Use LaunchedEffect to trigger the API call only once when the HomeScreen is first composed
    LaunchedEffect(Unit) {
        plantViewModel.fetchAllPlants()
        userViewModel.getUser(userData.userId)
    }

    val plantsList = plantViewModel.allPlants?.filter { it.user_id != userData.userId }
    val followed = userViewModel.user?.followedList

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        //topBar = {}
    ) {
        PagerScreen(
            onPlantClick = {},
            pagerState = pagerState,
            pages = pages,
            plantsList = plantsList,
            followed = followed,
            plantViewModel = plantViewModel,
            userViewModel = userViewModel,
            navController = navController
        )
    }

    /*
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar at the top
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Search for users...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            colors = TextFieldDefaults.colors(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn (modifier = Modifier.fillMaxSize()){
            item(){
                Text(
                    text = plantsList.toString()
                )
            }
        }
     */

    /*
    // Scrollable Feed List
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter the list based on search query
        val filteredUsers = userList.filter { it.username.contains(searchQuery, ignoreCase = true) }

        items(filteredUsers) { user ->
            GardenListItem(user = user, onClick = { onUserClick(user) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
*/
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SocialTopBar(
    pagerState: PagerState,
    onFilterClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        },
        /*
        actions = {
            if (pagerState.currentPage == SocialPage.ALL.ordinal) {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter_list_24dp),
                        contentDescription = stringResource(
                            id = R.string.menu_filter_by_grow_zone
                        )
                    )
                }
            }
        },
        */
        scrollBehavior = scrollBehavior
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PagerScreen(
    onPlantClick: (Plant) -> Unit,
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
                        onPlantClick = {
                            //onPlantClick(it)
                        },
                        plantsList = plantsList,
                        userViewModel = userViewModel,
                        plantViewModel = plantViewModel,
                        navController = navController
                    )
                }

                SocialPage.FOLLOWED -> {
                    FollowedFeed(
                        onPlantClick = onPlantClick,
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
    onPlantClick: (Plant) -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
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
    onPlantClick: () -> Unit,
    plantsList: List<FirestorePlant>?,
    plantViewModel: PlantViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {

    var searchQuery by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()

    ) {
        // Filter the list based on search query
        val userList = plantsList?.map { it.username }?.distinct()
        val filteredUsers = userList?.filter { it.contains(searchQuery, ignoreCase = true) }
        if (plantsList != null) {
            item{
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Search for users...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
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
                if(filteredUsers?.contains(plant.username) == true){
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
                } else {
                    Box(){
                        Text(text = "No user found")
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
                        /*
                        Text(
                            text = "Ficus benjamina, commonly known as weeping fig, benjamin fig or ficus tree, and often sold in stores as just ficus, is a species of flowering plant in the family Moraceae, native to Asia and Australia. It is the official tree of Bangkok. The species is also naturalized in the West Indies and in the states of Florida and Arizona in the United States. In its native range, its small fruit are favored by some birds.",
                            maxLines = 2,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                         */
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
                        /*
                        Text(
                            text = "Ficus benjamina, commonly known as weeping fig, benjamin fig or ficus tree, and often sold in stores as just ficus, is a species of flowering plant in the family Moraceae, native to Asia and Australia. It is the official tree of Bangkok. The species is also naturalized in the West Indies and in the states of Florida and Arizona in the United States. In its native range, its small fruit are favored by some birds.",
                            maxLines = 2,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                         */
                    }
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(darkGreen)
                    ) {
                        IconButton(
                            onClick = { /*TODO*/ },
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
