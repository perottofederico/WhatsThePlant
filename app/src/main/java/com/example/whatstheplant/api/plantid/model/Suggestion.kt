package com.example.whatstheplant.api.plantid.model

data class Suggestion(
    val details: Details,
    val id: String,
    val name: String,
    val probability: Double,
    val similar_images: List<SimilarImage>
)