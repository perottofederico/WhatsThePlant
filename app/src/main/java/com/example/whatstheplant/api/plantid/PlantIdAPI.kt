package com.example.whatstheplant.api.plantid

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.whatstheplant.api.plantid.model.MyLocation
import com.example.whatstheplant.api.plantid.model.Plant
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream

interface PlantIdentificationApi {
    @Headers("Content-Type: application/json")
    @POST("v3/identification")
    suspend fun identifyPlant(
        @Body body: JsonObject,
        @Query("details") details:String
    ): Plant //Response<JsonObject> //
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.plant.id/"

    val api: PlantIdentificationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlantIdentificationApi::class.java)
    }
}

fun encodeImageToBase64(image: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

suspend fun identifyPlantImage(apiKey: String, position : MyLocation?, imageBitmap: Bitmap): Plant? {
    return withContext(Dispatchers.IO) {
        try {
            // Encode the image to base64
            val base64Image = encodeImageToBase64(imageBitmap)
            // Create the request body
            val requestBody = JsonObject().apply {
                addProperty("api_key", apiKey)
                val images = JsonArray().apply { add(base64Image) }
                add("images", images)
                addProperty("classification_level", "all")
                //addProperty("health", "auto")
                addProperty("similar_images", true)
                position?.let {
                    addProperty("latitude", it.latitude)
                    addProperty("longitude", it.longitude)
                }
                //to make test fail
                 // addProperty("cake","lie")
            }
            val queryDetails = "common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,best_light_condition,best_soil_type,common_uses,cultural_significance,toxicity,best_watering"
            Log.d("PlantID", "RequestBody: $requestBody")

            // Make the API call using Retrofit
            val response = RetrofitInstance.api.identifyPlant(requestBody, details = queryDetails)

            if (response.status == "COMPLETED") {
                response // Return the response body
            } else {
                //val errorBody = response.errorBody()?.string()
                //Log.e("PlantID", "Error: ${response.code()} - $errorBody")
                null // Handle unsuccessful responses
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null // Handle exceptions
        }
    }
}

