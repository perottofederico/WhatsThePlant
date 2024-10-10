package com.example.whatstheplant.composables.tabs

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.example.whatstheplant.R
import com.example.whatstheplant.activities.SignOutHandler
import com.example.whatstheplant.composables.MyBigButton
import com.example.whatstheplant.composables.clickable
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.viewModel.PlantViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(
    userData: UserData,
    authClient: AuthClient,
    plantViewModel: PlantViewModel,
    signOutHandler: SignOutHandler
) {
    Log.d("PROFILE", userData.toString())
    val lifescope = rememberCoroutineScope()
    val markerLocations = plantViewModel.plantsList?.map { it.longitude?.let { lng ->
        it.latitude?.let { lat ->
            Point.fromLngLat(
                lng, lat
            )
        }
    } }
    var selectedImage by remember { mutableStateOf<Int?>(null) }
    var popupPosition by remember { mutableStateOf<Offset?>(null) }

    LazyColumn(
        state = rememberLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {


        item{
            Spacer(modifier = Modifier.height(32.dp))
            if(userData.profilePictureUrl != "null") {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_profile),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        item{
            Spacer(modifier = Modifier.height(28.dp))

            userData.username?.let {
                OutlinedTextField(
                    value = it,
                    onValueChange = {},
                    label = {Text("Name")},
                    leadingIcon = {Icon( imageVector = Icons.Default.Person, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            userData.email?.let {
                OutlinedTextField(
                    value = it,
                    onValueChange = {},
                    label = {Text("Email")},
                    leadingIcon = { Icon(imageVector = Icons.Default.MailOutline, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        item{
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Column {
                    Text("Total Plants Scanned")
                    Text(
                        plantViewModel.scannedPlantsCount.value.toString(),
                        textAlign = TextAlign.Center
                    )
                }
                Column {
                    Text("Plants In Garden")
                    Text(
                        plantViewModel.plantsList?.size.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item{
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(5.0)
                    if (!markerLocations.isNullOrEmpty()) {
                        center(markerLocations[0])
                    }
                    pitch(0.0)
                    bearing(0.0)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 32.dp, end = 16.dp)
                    .height(300.dp)
            ) {
                MapboxMap(
                    Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                    popupPosition = Offset(event.x, event.y)
                                }
                            }
                            false // do not consume the event so it does propagate further
                        },
                    mapViewportState = mapViewportState
                ){
                    markerLocations?.forEachIndexed { index, loc->
                        if (loc != null) {
                            if(selectedImage != null){
                                val imgUrl = plantViewModel.plantsList?.get(index)?.img_url
                                ViewAnnotation(
                                    options = viewAnnotationOptions {
                                        // View annotation is placed at the specific geo coordinate
                                        geometry(loc)
                                        annotationAnchor {
                                            anchor(ViewAnnotationAnchor.TOP_LEFT)
                                        }
                                    }
                                ) {
                                    // Insert the content of the ViewAnnotation
                                    if (imgUrl != null) {
                                        ViewAnnotationContent(imgUrl)
                                    }
                                }
                            }

                            //AddMarker(loc)
                            val marker = rememberIconImage(
                                key = R.drawable.red_marker,
                                painter = painterResource(R.drawable.red_marker)
                            )
                            PointAnnotation(point = loc, onClick = {
                                selectedImage = if(selectedImage == index) null else index
                                true
                            }
                            ) {
                                // specify the marker image
                                iconImage = marker
                            }
                        }
                    }

                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            //Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 88.dp),
                horizontalArrangement = Arrangement.Center
            ){
                MyBigButton(text = "Sign Out", onClick = {
                    lifescope.launch {
                        authClient.signOut()
                        signOutHandler.onSignOut()
                    }
                })
            }
        }
    }
}

@Composable
 fun ViewAnnotationContent(imgUrl : String) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
    ) {
        AsyncImage(
            model = imgUrl,
            contentDescription = "Marker Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.default_profile),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = "Username",
            onValueChange = {},
            label = {Text("Name")},
            leadingIcon = {Icon( imageVector = Icons.Default.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = "email",
            onValueChange = {},
            label = {Text("Email")},
            leadingIcon = { Icon(imageVector = Icons.Default.MailOutline, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Column {
                Text("Total Plants Scanned")
                Text(
                    "2",
                    textAlign = TextAlign.Center
                )
            }
            Column {
                Text("Plants In Garden")
                Text(
                    "2",
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { /*TODO: Implement edit profile functionality*/ }) {
                Text(text = "SIGN OUT")
            }
        }
    }
}