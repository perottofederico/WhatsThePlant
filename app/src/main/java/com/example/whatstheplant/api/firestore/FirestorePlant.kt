package com.example.whatstheplant.api.firestore

import com.example.whatstheplant.api.plantid.model.SimilarImage
import com.example.whatstheplant.api.plantid.model.Taxonomy

data class FirestorePlant(
    val user_id : String,
    val username : String,
    val plant_id : String?,
    val img_url: String?,
    val similarImages: List<String>,
    val latitude : Double?,
    val longitude : Double?,
    val name : String?,
    val commonNames : String,
    val description : String?,
    val bestLightCondition : String?,
    val bestSoilType : String?,
    val bestWatering : String?,
    val taxonomy: Taxonomy?,
    val edibleParts : String?,
    val commonUses: String?,
    val culturalSignificance : String?,
    val toxicity : String?
)
