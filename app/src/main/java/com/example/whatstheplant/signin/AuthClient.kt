package com.example.whatstheplant.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.whatstheplant.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class AuthClient (
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val signInViewModel: SignInViewModel
) {
    private val auth = Firebase.auth


    //GOOGLE
    //-----------------------------------------------------------------------------------------------------//
    suspend fun signIn(): IntentSender? {
        val result =try{
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun buildSignInRequest(): BeginSignInRequest{
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            //.setAutoSelectEnabled(true)
            .build()
    }

    suspend fun signInFromIntent(intent: Intent): SignInResult{
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try{
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl =photoUrl.toString(),
                        email = email,
                        emailVerified = isEmailVerified
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl.toString(),
            email = email
        )
    }


    //EMAIL
    //---------------------------------------------------------------------------------------------//
    suspend fun signInWithEmailAndPassword(email: String, password: String) = try {
        val user = auth.signInWithEmailAndPassword(email,password).await().user
        SignInResult(
            data = user?.run {
                UserData(
                    userId = uid,
                    username = displayName,
                    profilePictureUrl = photoUrl?.toString(),
                    email = email,
                )
            },
            errorMessage = null
        )
    } catch(e: Exception) {
        e.printStackTrace()
        if(e is CancellationException) throw e
        val error: String = when(e) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
            is FirebaseAuthInvalidUserException -> "User not found"
            else -> "Authentication failed. Please try again."
        }
        SignInResult(
            data = null,
            errorMessage = error
        )
    }



    suspend fun signUpWithEmailAndPassword(
        email: String, password: String, username: String
    ) = try {
        val user  = auth.createUserWithEmailAndPassword(email, password).await().user
        /*
        if (user != null) {
            val userResponse =  userRemoteService.get(token=token,id= user.uid)
            if (userResponse.code() == 204) {
                val data = gson.toJson(
                    UserData(
                        id = user.uid,
                        email = user.email,
                        profilePictureUrl = user.photoUrl?.toString(),
                        username = username
                    )
                )
                userRemoteService.create(token = token, body = data)
            }
        }
        */
        if(user != null){
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user.updateProfile(userProfileChangeRequest)
        }
        SignInResult(
            data = user?.run {
                UserData(
                    userId = uid,
                    username = username,
                    profilePictureUrl = photoUrl?.toString(),
                    email = email
                )
            },
            errorMessage = null
        )
    } catch(e: Exception) {
        e.printStackTrace()
        if(e is CancellationException) throw e
        val error: String = when(e) {
            is FirebaseAuthWeakPasswordException -> "The password is too weak. Please choose a stronger password."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email format. Please enter a valid email address."
            is FirebaseAuthUserCollisionException -> "An account with this email address already exists. Please use a different email."
            else -> "Registration failed. Please try again."
        }
        SignInResult(
            data = null,
            errorMessage = error
        )
    }



    //---------------------------------------------------------------------------------------------//
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
            SignInResult(
                data = null,
                errorMessage = null
            )

        } catch (e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
        Log.d("AUTHCLIENT", "Signed Out")
    }
}