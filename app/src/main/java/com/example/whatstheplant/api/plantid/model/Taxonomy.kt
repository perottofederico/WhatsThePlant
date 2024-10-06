package com.example.whatstheplant.api.plantid.model

data class Taxonomy(
    val `class`: String,
    val family: String,
    val genus: String,
    val kingdom: String,
    val order: String,
    val phylum: String
)