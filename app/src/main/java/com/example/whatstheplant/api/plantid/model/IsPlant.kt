package com.example.whatstheplant.api.plantid.model

data class IsPlant(
    val binary: Boolean,
    val probability: Double,
    val threshold: Double
)