package com.example.whatstheplant.signin

data class GoogleSignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
