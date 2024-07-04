package com.example.whatstheplant.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplant.composables.MainScreen
import com.example.whatstheplant.ui.theme.WhatsThePlantTheme
import com.example.whatstheplant.ui.theme.green


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class BaseActivity : ComponentActivity() {

    private var user_id: String = ""
    private var username: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        //It takes userId and username from the Intent
        user_id = bundle!!.getString("user_id")!!
        username = bundle!!.getString("username")!!

        setContent {
            WhatsThePlantTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize().background(green)){
                    MainScreen(navController = navController)
                }
            }
        }
    }
}
