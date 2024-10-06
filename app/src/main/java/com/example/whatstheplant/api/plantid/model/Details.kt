package com.example.whatstheplant.api.plantid.model

data class Details(
    val best_light_condition: String,
    val best_soil_type: String,
    val best_watering: String,
    val common_names: List<String>,
    val common_uses: String,
    val cultural_significance: String,
    val description: Description,
    val edible_parts: Any,
    val entity_id: String,
    val gbif_id: Int,
    val image: Image,
    val inaturalist_id: Int,
    val language: String,
    val rank: String,
    val synonyms: List<String>,
    val taxonomy: Taxonomy,
    val toxicity: String,
    val url: String,
    val watering: Watering
)