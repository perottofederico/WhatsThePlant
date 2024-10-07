package com.example.whatstheplant.api.firestore

data class FirestoreTask(
    val userId : String,
    val plantId : String,
    val plantName :String,
    val taskId : String,
    val type : String,
    val startDate: String,
    val endDate : String,
    val frequency : Int
)
