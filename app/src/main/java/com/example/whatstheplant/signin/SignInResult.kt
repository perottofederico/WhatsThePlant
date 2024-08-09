package com.example.whatstheplant.signin

data class SignInResult (
    val data : UserData?,
    val errorMessage: String?
)


data class UserData(
    val userId: String,
    val email: String? = null,
    val username: String?,
    val profilePictureUrl: String? = null,
    val emailVerified: Boolean = false
)