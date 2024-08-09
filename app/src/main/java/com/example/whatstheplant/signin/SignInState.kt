package com.example.whatstheplant.signin

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
