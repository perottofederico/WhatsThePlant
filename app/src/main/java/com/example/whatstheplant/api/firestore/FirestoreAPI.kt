package com.example.whatstheplant.api.firestore

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FirestoreApiInterface {
    @Headers("Content-Type: application/json")

    //Users
    @POST("add_user")
    fun addUSer(@Body user : FirestoreUser) : Call<Void>
    @GET("users/{userId}")
    fun getUser(@Path("userId") userId: String) : Call<FirestoreUser>
    @PUT("users/{userId}")
    fun addFollow(@Path("userId") userId: String, @Body followedUser: String) : Call<Void>

    // Plants
    @POST("add_plant")
    fun addPlant(@Body plant: FirestorePlant) : Call<Void>
    @GET("plants/{userId}")
    fun getPlantsByUser(@Path("userId") userId: String): Call<List<FirestorePlant>>
    @GET("plants")
    fun getAllPlants(): Call<List<FirestorePlant>>
    @DELETE("plants/{plantId}")
    fun deletePlant(@Path("plantId") plantId: String) : Call<Void>

    //Tasks
    @POST("add_task")
    fun addTask(@Body task: FirestoreTask)  : Call<Void>
    @GET("tasks/{userId}")
    fun getTasksByUser(@Path("userId") userId: String): Call<List<FirestoreTask>>
    @DELETE("tasks/{taskId}")
    fun deleteTask(@Path("taskId") taskId: String) : Call<Void>

}

object Retrofitclient {
    private const val BASE_URL = "http://MushyWeirdo.pythonanywhere.com/"

    val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

val firestoreApiInterface = Retrofitclient.retrofit.create(FirestoreApiInterface::class.java)