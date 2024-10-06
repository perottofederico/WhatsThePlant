package com.example.whatstheplant.api.plantid.model

data class SimilarImage(
    val citation: String,
    val id: String,
    val license_name: String,
    val license_url: String,
    val similarity: Double,
    val url: String,
    val url_small: String
)