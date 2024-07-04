package com.example.whatstheplant.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplant.signin.AuthViewModel
import com.example.whatstheplant.composables.LoginScreen
import com.example.whatstheplant.composables.SignupScreen
import com.example.whatstheplant.ui.theme.WhatsThePlantTheme
import com.google.firebase.auth.FirebaseAuth

var authViewModel: AuthViewModel? = null

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatsThePlantTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val auth = FirebaseAuth.getInstance()
                    auth.currentUser?.let {
                        auth.signOut()
                    }
                    authViewModel = viewModel()
                    NavHost(navController = navController, startDestination = "Login") {
                        composable("Login"){
                            LoginScreen(authViewModel!!, navController = navController)
                        }
                        composable("Signup"){
                            SignupScreen(authViewModel!!, navController = navController)
                        }
                    }
                }
            }
        }
    }
}
