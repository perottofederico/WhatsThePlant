package com.example.whatstheplant.composables

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.whatstheplant.activities.SignOutHandler
import com.example.whatstheplant.api.firestore.FirestoreUser
import com.example.whatstheplant.api.plantid.model.Plant
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.composables.tabs.HomeScreen
import com.example.whatstheplant.composables.tabs.ProfileScreen
import com.example.whatstheplant.composables.tabs.SearchScreen
import com.example.whatstheplant.composables.tabs.camera.CameraScreen
import com.example.whatstheplant.composables.tabs.SocialScreen
import com.example.whatstheplant.datastore.PlantRepository

import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.SignInViewModel
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.viewModel.PlantViewModel
import com.example.whatstheplant.viewModel.PlantViewModelFactory
import com.example.whatstheplant.viewModel.UserViewModel

@Composable
fun NavigationScreens(
    userData: UserData,
    authClient: AuthClient,
    navController: NavHostController,
    signOutHandler: SignOutHandler
) {
    val viewModel = viewModel<SignInViewModel>()
    val repository = PlantRepository(LocalContext.current)
    val plantViewModel: PlantViewModel = viewModel(factory = PlantViewModelFactory(repository))
    val userViewModel: UserViewModel = viewModel()
    val apiResult = remember {
        mutableStateOf<Plant?>(null)
    }

    LaunchedEffect (Unit){
        userViewModel.addUser(FirestoreUser(
            userId = userData.userId,
            userName = userData.username!!,
            email = userData.email!!,
            profilePic = userData.profilePictureUrl!!,
            followedList = emptyList()
        ))
    }

    NavHost(navController, startDestination = NavItem.Home.path) {
        composable(NavItem.Home.path) {
            HomeScreen(
                navController = navController,
                userData = userData,
                plantViewModel = plantViewModel
            )
        }
        composable(NavItem.Search.path) { SearchScreen() }
        composable(NavItem.Camera.path) { CameraScreen(
            navController,
            apiResult,
            plantViewModel
        ) }
        composable(NavItem.Feed.path) { SocialScreen(
            navController,
            userData = userData,
            plantViewModel = plantViewModel,
            userViewModel = userViewModel
        ) }
        composable(NavItem.Profile.path) {
            ProfileScreen(
                userData = userData,
                authClient = authClient,
                viewModel = viewModel,
                plantViewModel = plantViewModel,
                signOutHandler = signOutHandler
            )
        }

        composable(NavItem.ApiResult.path) {
            ApiResultScreen(
                apiResult = apiResult,
                userData = userData,
                plantViewModel = plantViewModel,
                navController = navController
            )
        }

        composable(NavItem.PlantDetail.path) {
            PlantDetail(plantViewModel, navController)
        }

        composable(NavItem.OtherUserPlantDetail.path){
            OtherUserPlantDetail(plantViewModel, navController)
        }

        composable("${NavItem.OtherUserGarden.path}/{userId}"){ navBackStackEntry->
            val userId = navBackStackEntry.arguments?.getString("userId")
            if (userId != null) {
                OtherUserGarden(
                    navController = navController,
                    plantViewModel = plantViewModel,
                    userId = userId
                )
            }
        }
    }
}