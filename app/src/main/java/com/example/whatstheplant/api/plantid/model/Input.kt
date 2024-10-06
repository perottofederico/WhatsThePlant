package com.example.whatstheplant.api.plantid.model

data class Input(
    val datetime: String,
    val images: List<String>,
    val latitude: Double,
    val longitude: Double,
    val similar_images: Boolean
)