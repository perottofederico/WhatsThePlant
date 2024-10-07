package com.example.whatstheplant.composables

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import com.example.whatstheplant.activities.SignOutHandler
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.ui.theme.green

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    userData: UserData,
    authClient: AuthClient,
    navController: NavHostController,
    signOutHandler: SignOutHandler
) {

    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

    Scaffold(bottomBar = {
            BottomNavigationBar(
                navController = navController,
                bottomBarState = bottomBarState
            )
    }) {
        NavigationScreens(
            userData = userData,
            authClient = authClient,
            navController = navController,
            signOutHandler = signOutHandler
        )
    }
}