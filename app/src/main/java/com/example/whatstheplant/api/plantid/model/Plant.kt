package com.example.whatstheplant.api.plantid.model

data class Plant(
    val access_token: String,
    val completed: Double,
    val created: Double,
    val custom_id: Any,
    val input: Input,
    val model_version: String,
    val result: Result,
    val sla_compliant_client: Boolean,
    val sla_compliant_system: Boolean,
    val status: String
)