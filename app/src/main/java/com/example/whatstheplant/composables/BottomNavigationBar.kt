package com.example.whatstheplant.composables


import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.whatstheplant.activities.CameraActivity
import com.example.whatstheplant.nav.NavItem
import com.example.whatstheplant.ui.theme.green

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    bottomBarState: MutableState<Boolean>
    ){
    val context = LocalContext.current
    val bottomAppBarState = rememberSaveable {
        mutableStateOf(true)
    }
    val navItems =
        listOf(NavItem.Home, NavItem.Search, NavItem.Camera, NavItem.List, NavItem.Profile)
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = {it}),
        exit = slideOutVertically(targetOffsetY = {it}),
        content = {
            NavigationBar(
                containerColor = green,
                contentColor = Color.White
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color.White
                        ),
                        alwaysShowLabel = true,
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            bottomBarState.value = item.title != "Camera"

                            //if(item.title == "Camera"){
                                //val intent = Intent(context, CameraActivity::class.java)
                                //context.startActivity(intent)
                            //} else {
                                selectedItem = index
                                navController.navigate(item.path) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            //}
                        }
                    )
                }
            }
        }
    )
}