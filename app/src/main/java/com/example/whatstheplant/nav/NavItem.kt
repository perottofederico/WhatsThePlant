package com.example.whatstheplant.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

sealed class NavItem {
    object Login:
        Item(path = NavPath.LOGIN.toString(), title = NavTitle.LOGIN, icon = Icons.AutoMirrored.Filled.Login)

    object Signup:
        Item(path = NavPath.SIGNUP.toString(), title = NavTitle.SIGNUP, icon = Icons.Default.AppRegistration)

    object Home :
        Item(path = NavPath.HOME.toString(), title = NavTitle.HOME, icon = Icons.AutoMirrored.Filled.List)

    object Search :
        Item(path = NavPath.SEARCH.toString(), title = NavTitle.SEARCH, icon = Icons.Default.Search)

    object Camera :
        Item(path = NavPath.CAMERA.toString(), title = NavTitle.CAMERA, icon = Icons.Default.CameraAlt)

    object List :
        Item(path = NavPath.LIST.toString(), title = NavTitle.LIST, icon = Icons.AutoMirrored.Filled.List)

    object Profile :
        Item(path = NavPath.PROFILE.toString(), title = NavTitle.PROFILE, icon = Icons.Default.Person)

    object Exit :
            Item(path = NavPath.EXIT.toString(), title = NavTitle.EXIT, icon = Icons.AutoMirrored.Filled.List)
}