package com.example.whatstheplant.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Yard

sealed class NavItem {
    object Login:
        Item(path = NavPath.LOGIN.toString(), title = NavTitle.LOGIN, icon = Icons.AutoMirrored.Filled.Login)

    object Signup:
        Item(path = NavPath.SIGNUP.toString(), title = NavTitle.SIGNUP, icon = Icons.Default.AppRegistration)

    object Home :
        Item(path = NavPath.HOME.toString(), title = NavTitle.HOME, icon = Icons.Default.Yard)

    object Search :
        Item(path = NavPath.CALENDAR.toString(), title = NavTitle.CALENDAR, icon = Icons.Default.CalendarMonth)

    object Camera :
        Item(path = NavPath.CAMERA.toString(), title = NavTitle.CAMERA, icon = Icons.Default.CameraAlt)

    object Feed :
        Item(path = NavPath.FEED.toString(), title = NavTitle.FEED, icon = Icons.Default.Forum)

    object Profile :
        Item(path = NavPath.PROFILE.toString(), title = NavTitle.PROFILE, icon = Icons.Default.Person)

    object ApiResult:
            Item(path = NavPath.APIRESULT.toString(), title = NavTitle.APIRESULT, icon = Icons.AutoMirrored.Filled.List)

    object PlantDetail:
            Item(path = NavPath.PLANTDETAIL.toString(), title = NavTitle.PLANTDETAIL, icon = Icons.Default.Search)

    object OtherUserPlantDetail:
        Item(path = NavPath.OTHERUSERPLANTDETAIL.toString(), title = NavTitle.OTHERUSERPLANTDETAIL, icon = Icons.Default.Search)

    object OtherUserGarden:
        Item(path = NavPath.OTHERUSERGARDEN.toString(), title = NavTitle.OTHERUSERGARDEN, icon = Icons.Default.Search)

    object Exit :
            Item(path = NavPath.EXIT.toString(), title = NavTitle.EXIT, icon = Icons.AutoMirrored.Filled.List)
}