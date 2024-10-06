package com.example.whatstheplant.composables.tabs

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.whatstheplant.R
import com.example.whatstheplant.activities.SignOutHandler
import com.example.whatstheplant.composables.MyBigButton
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.SignInViewModel
import com.example.whatstheplant.signin.UserData
import com.example.whatstheplant.viewModel.PlantViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userData: UserData,
    authClient: AuthClient,
    viewModel: SignInViewModel,
    plantViewModel: PlantViewModel,
    signOutHandler: SignOutHandler
) {
    Log.d("PROFILE", userData.toString())
    val context = LocalContext.current
    val lifescope = rememberCoroutineScope()
    val markerLocations = plantViewModel.plantsList?.map { it.longitude?.let { lng ->
        it.latitude?.let { lat ->
            Point.fromLngLat(
                lng, lat
            )
        }
    } }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 32.dp, end = 16.dp)
                    .height(300.dp)
                //.verticalScroll(rememberScrollState())
            ) {
                MapboxMap(
                    Modifier
                        .fillMaxSize(),
                    mapViewportState = rememberMapViewportState {
                        setCameraOptions {
                            zoom(5.0)
                            center(markerLocations?.get(0))
                            pitch(0.0)
                            bearing(0.0)
                        }
                    }
                )
                {
                    markerLocations?.forEach { loc->
                        if (loc != null) {
                            AddMarker(loc)
                        }
                    }

                }
            }
        }

        item {
            //Spacer(modifier = Modifier.height(16.dp))
            //Spacer(modifier = Modifier.weight(1f))

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
fun AddMarker(loc: Point) {
    val marker = rememberIconImage(
        key = R.drawable.red_marker,
        painter = painterResource(R.drawable.red_marker)
    )
    PointAnnotation(point = loc) {
        // specify the marker image
        iconImage = marker
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