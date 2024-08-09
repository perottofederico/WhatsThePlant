package com.example.whatstheplant.composables

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.whatstheplant.R
import com.example.whatstheplant.activities.WhatsThePlant
import com.example.whatstheplant.signin.AuthClient
import com.example.whatstheplant.signin.SignInState
import com.example.whatstheplant.signin.SignInViewModel
import com.example.whatstheplant.ui.theme.gray
import com.example.whatstheplant.ui.theme.green
import com.example.whatstheplant.ui.theme.superDarkGreen
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    state: SignInState = SignInState(),
    onSignInClick: () -> Unit,
    authClient: AuthClient,
    authViewModel: SignInViewModel,
    //googleIntentLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>? = null,
    navController: NavController
) {

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Logo()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "What's the plant",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(),
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal
                ),
                color = green,
                textAlign = TextAlign.Center
            )

            // email text field
            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value = it
                },
                label = { MyText("Email") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = superDarkGreen,
                    cursorColor = superDarkGreen,
                    focusedBorderColor = superDarkGreen
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            //password text field
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                },
                label = { MyText("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = superDarkGreen,
                    cursorColor = superDarkGreen,
                    focusedBorderColor = superDarkGreen
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Login button
            MyBigButton(text="Login", onClick = {
                authViewModel.viewModelScope.launch {
                    val signInResult = authClient.signInWithEmailAndPassword(
                        emailState.value,
                        passwordState.value
                    )
                    authViewModel.onSignInResult(
                        signInResult,
                        context
                    )
                    val userData = authClient.getSignedInUser()
                    val intent = Intent(context, WhatsThePlant::class.java)
                    //intent.putExtra("user_id", userData!!.userId)
                    //intent.putExtra("username", userData!!.username)
                    context.startActivity(intent)
                }
            })

            TextButton(onClick = { navController.navigate("SignUp") }) {
                MyText(
                    "Don't have an account? Register"
                )
            }

            // OAuth Options
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    thickness = 1.dp,
                    color = gray
                )
                Text(
                    text = "Or",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 20.sp
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    thickness = 1.dp,
                    color = gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onSignInClick,
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .padding(4.dp)
                        .border(
                            width = 2.dp,
                            color = Color(android.graphics.Color.parseColor("#d2d2d2")),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_svg),
                        contentDescription = "Google Logo",
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                /*
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .padding(4.dp)
                        .border(
                            width = 2.dp,
                            color = Color(android.graphics.Color.parseColor("#d2d2d2")),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.facebook_svg),
                        contentDescription = "Google Logo",
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                 */
            }
        }
    }
}