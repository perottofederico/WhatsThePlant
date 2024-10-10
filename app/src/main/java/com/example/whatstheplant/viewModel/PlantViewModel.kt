package com.example.whatstheplant.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatstheplant.api.firestore.FirestorePlant
import com.example.whatstheplant.api.firestore.firestoreApiInterface
import com.example.whatstheplant.datastore.PlantRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantViewModel(private val repository: PlantRepository) : ViewModel() {
    private val _selectedPlant = MutableLiveData<FirestorePlant>()
    val selectedPlant: LiveData<FirestorePlant> get() = _selectedPlant

    fun setSelectedPlant(plant: FirestorePlant) {
        Log.d("PlantViewModel", "Setting selected plant: $plant")
        _selectedPlant.value = plant
    }

    var plantsList by mutableStateOf<List<FirestorePlant>?>(null)
        private set

    var allPlants by mutableStateOf<List<FirestorePlant>?>(null)
        private set

    fun addPlant(plant: FirestorePlant) {
        val call = firestoreApiInterface.addPlant(plant)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchPlantList(userId = plant.user_id)
                    //Log.d("AddPlant", "Success")
                } else {
                    // Handle error response from server
                    Log.d(
                        "AddPlant",
                        "Error: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("AddPlant", "Failure: ${t.message}")
            }
        })
    }
    /*
    fun fetchPlant(plantId : String){
        val call = firestoreApiInterface.getPlant(plantId)
        call.enqueue(object : Callback<FirestorePlant> {
            override fun onResponse(call: Call<FirestorePlant>, response: Response<FirestorePlant>) {
                if (response.isSuccessful) {
                    response.body()?.let { setSelectedPlant(it) }
                    //Log.d("fetchPlant", "Success")
                } else {
                    // Handle error response from server
                    Log.d(
                        "Fetch plant",
                        "Error: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<FirestorePlant>, t: Throwable) {
                Log.d("Fetch plant", "Failure: ${t.message}")
            }
        })
    }
     */

    fun fetchPlantList(userId: String) {
        firestoreApiInterface.getPlantsByUser(userId)
            .enqueue(object : Callback<List<FirestorePlant>> {
                override fun onResponse(
                    call: Call<List<FirestorePlant>>,
                    response: Response<List<FirestorePlant>>
                ) {
                    if (response.isSuccessful) {
                        val plants = response.body()
                        if (plants != null) {
                            // Successfully received a list of plants, update the state
                            Log.d(
                                "fetchPlantList",
                                "Fetched ${plants.size} plants from API."
                            )
                            plantsList = plants//.sortedByDescending { it.created_at }
                        } else {
                            // Handle case where the response body is null (unexpected)
                            Log.w(
                                "fetchPlantList",
                                "No plants returned by the API (null body)."
                            )
                            plantsList = emptyList()  // Set to an empty list in case of null
                        }
                    } else {
                        // Handle non-success HTTP status codes (4xx, 5xx)
                        Log.e(
                            "fetchPlantList",
                            "API response error: ${response.code()} - ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<FirestorePlant>>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
    }

    fun fetchAllPlants() {
        firestoreApiInterface.getAllPlants().enqueue(object : Callback<List<FirestorePlant>> {
            override fun onResponse(
                call: Call<List<FirestorePlant>>,
                response: Response<List<FirestorePlant>>
            ) {
                if (response.isSuccessful) {
                    val plants = response.body()
                    if (plants != null) {
                        // Successfully received a list of plants, update the state
                        Log.d(
                            "fetchAllPlants",
                            "Fetched ${plants.size} plants from API."
                        )
                        allPlants = plants
                    } else {
                        // Handle case where the response body is null (unexpected)
                        Log.w(
                            "fetchAllPlants",
                            "No plants returned by the API (null body)."
                        )
                        allPlants = emptyList()  // Set to an empty list in case of null
                    }
                } else {
                    // Handle non-success HTTP status codes (4xx, 5xx)
                    Log.e(
                        "fetchAllPlants",
                        "API response error: ${response.code()} - ${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<FirestorePlant>>, t: Throwable) {
                Log.e("API Error in fetchAll", "Failure: ${t.message}")
            }
        })
    }

    fun deletePlant(userId: String, plantId: String) {
        firestoreApiInterface.deletePlant(plantId = plantId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        fetchPlantList(userId = userId)
                        Log.d("PLANTVIEWMODEL", "Plant removed successfully")
                    } else {
                        Log.w(
                            "PLANTVIEWMODEL",
                            "Error: ${response.code()} - ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("API Error in deletePlant", "Failure: ${t.message}")
                }
            })
    }



    private val _scannedPlantsCount = mutableIntStateOf(0)
    val scannedPlantsCount: State<Int> = _scannedPlantsCount

    init {
        // Observe the stored scanned count
        viewModelScope.launch {
            repository.scannedCountFlow.collect { count ->
                _scannedPlantsCount.intValue = count
            }
        }
    }

    // Increment and persist the scanned count
    fun incrementScannedCount() {
        viewModelScope.launch {
            val newCount = _scannedPlantsCount.intValue + 1
            _scannedPlantsCount.intValue = newCount
            repository.saveScannedCount(newCount)
        }
    }

}