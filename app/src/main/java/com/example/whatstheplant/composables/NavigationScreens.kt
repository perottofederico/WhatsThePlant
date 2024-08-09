package com.example.whatstheplant.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.whatstheplant.activities.SignOutHandler
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.composables.tabs.HomeScreen
import com.example.whatstheplant.composables.tabs.ProfileScreen
import com.example.whatstheplant.composables.tabs.SearchScreen
import com.example.whatstheplant.composables.tabs.camera.CameraScreen
import com.example.whatstheplant.composables.tabs.ListScreen
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.SignInViewModel
import com.example.whatstheplant.signin.UserData

@Composable
fun NavigationScreens(
    userData: UserData,
    authClient: AuthClient,
    navController: NavHostController,
    signOutHandler: SignOutHandler
    ) {
    val viewModel = viewModel<SignInViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavHost(navController, startDestination = NavItem.Home.path) {
        composable(NavItem.Home.path) { HomeScreen() }
        composable(NavItem.Search.path) { SearchScreen() }
        composable(NavItem.Camera.path) { CameraScreen() }
        composable(NavItem.List.path) { ListScreen() }
        composable(NavItem.Profile.path) { ProfileScreen(
            userData = userData,
            authClient = authClient,
            viewModel = viewModel,
            signOutHandler = signOutHandler
        ) }
    }
}