package com.example.whatstheplant.composables.tabs

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import com.example.whatstheplant.ui.theme.superDarkGreen
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userData: UserData,
    authClient: AuthClient,
    viewModel: SignInViewModel,
    signOutHandler: SignOutHandler
) {
    Log.d("PROFILE", userData.toString())
    val context = LocalContext.current
    val lifescope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

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

        //Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom=70.dp),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom=10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { /*TODO: Implement edit profile functionality*/ }) {
                Text(text = "SIGN OUT")
            }
        }
    }
}