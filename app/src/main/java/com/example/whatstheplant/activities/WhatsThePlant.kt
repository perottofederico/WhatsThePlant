package com.example.whatstheplant.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplant.composables.MainScreen
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.SignInViewModel
import com.example.whatstheplant.ui.theme.WhatsThePlantTheme
import com.example.whatstheplant.ui.theme.green
import com.google.android.gms.auth.api.identity.Identity


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class WhatsThePlant : ComponentActivity(), SignOutHandler {
    private val signInViewModel: SignInViewModel by viewModels()

    private val authClient by lazy {
        AuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            signInViewModel = signInViewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WhatsThePlantTheme {
                val navController = rememberNavController()
                val userData = authClient.getSignedInUser()
                Surface(modifier = Modifier.fillMaxSize().background(green))
                {
                    userData?.let {
                        MainScreen(
                            userData = it,
                            authClient = authClient,
                            navController = navController,
                            signOutHandler = this
                        )
                    }
                }
            }
        }
    }
    override fun onSignOut() {
        finish()
    }
}



interface SignOutHandler {
    fun onSignOut()
}