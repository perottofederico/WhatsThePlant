package com.example.whatstheplant.api.firestore

data class FirestoreUser(
    val userId: String,
    val userName : String,
    val email: String,
    val profilePic:String,
    val followedList: List<String>
)
