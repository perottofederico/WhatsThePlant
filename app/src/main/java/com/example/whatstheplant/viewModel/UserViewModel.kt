package com.example.whatstheplant.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.firestore.FirestoreUser
import com.example.whatstheplant.api.firestore.firestoreApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel() {

    var user by mutableStateOf<FirestoreUser?>(null)
        private set

    fun addUser(user: FirestoreUser) {
        val call = firestoreApiInterface.addUSer(user)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("addUser", "${response.code()} - ${response.message()} - ${response.body()}")
                } else {
                    // Handle error response from server
                    Log.d("addUser", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("addUser", "Failure: ${t.message}")
            }
        })
    }

    fun getUser(userId: String){
        val call = firestoreApiInterface.getUser(userId)
        call.enqueue(object : Callback<FirestoreUser> {
            override fun onResponse(call: Call<FirestoreUser>, response: Response<FirestoreUser>) {
                if (response.isSuccessful) {
                    user = response.body()
                    Log.d("getUser", "${response.body()}")
                } else {
                    // Handle error response from server
                    Log.d("getUser", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<FirestoreUser>, t: Throwable) {
                Log.e("API Error", "Failure: ${t.message}")
            }
        })
    }

    fun addFollow(userId: String, followedUser: String){
        val call = firestoreApiInterface.addFollow(userId, followedUser)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("AddFollow", "${response.code()} - ${response.message()} - ${response.body()}  ")
                    getUser(userId = userId)
                } else {
                    Log.d("AddFollow", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API Error", "Failure: ${t.message}")
            }
        })
    }

}