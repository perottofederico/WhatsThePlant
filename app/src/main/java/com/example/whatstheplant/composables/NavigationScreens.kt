package com.example.whatstheplant.composables

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplant.activities.authViewModel
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.composables.tabs.HomeScreen
import com.example.whatstheplant.composables.tabs.ProfileScreen
import com.example.whatstheplant.composables.tabs.SearchScreen
import com.example.whatstheplant.composables.tabs.CameraScreen
import com.example.whatstheplant.composables.tabs.ListScreen

@Composable
fun NavigationScreens(navController: NavHostController) {
    //val navController = rememberNavController()
    authViewModel = viewModel()
    NavHost(navController, startDestination = NavItem.Home.path) {
        composable(NavItem.Login.path) { LoginScreen(authViewModel!!, navController) }
        composable(NavItem.Signup.path) { SignupScreen(authViewModel!!, navController) }
        composable(NavItem.Home.path) { HomeScreen() }
        composable(NavItem.Search.path) { SearchScreen() }
        composable(NavItem.Camera.path) { CameraScreen() }
        composable(NavItem.List.path) { ListScreen() }
        composable(NavItem.Profile.path) { ProfileScreen() }
    }
}