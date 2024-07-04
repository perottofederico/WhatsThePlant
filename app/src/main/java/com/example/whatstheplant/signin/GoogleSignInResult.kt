package com.example.whatstheplant.signin

data class GoogleSignInResult (
    val data : UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)