package com.example.whatstheplant.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.whatstheplant.api.firestore.FirestoreTask
import com.example.whatstheplant.api.firestore.firestoreApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskViewModel : ViewModel() {

    var tasksList by mutableStateOf<List<FirestoreTask>?>(null)
        private set

    fun addTask(task: FirestoreTask) {
        val call = firestoreApiInterface.addTask(task)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchTaskList(userId = task.userId)
                } else {
                    Log.w(
                        "TASKVIEWMODEL",
                        "Error: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("TASKVIEWMODEL", "Failure: ${t.message}")
            }
        })
    }

    fun fetchTaskList(userId: String) {
        firestoreApiInterface.getTasksByUser(userId)
            .enqueue(object : Callback<List<FirestoreTask>> {
                override fun onResponse(
                    call: Call<List<FirestoreTask>>,
                    response: Response<List<FirestoreTask>>
                ) {
                    if (response.isSuccessful) {
                        val tasks = response.body()
                        if (tasks != null) {
                            // Successfully received a list of plants, update the state
                            Log.d(
                                "TASKVIEWMODEL",
                                "Fetched ${tasks.size} tasks from API."
                            )
                            tasksList = tasks
                        } else {
                            // Handle case where the response body is null (unexpected)
                            Log.w(
                                "TASKVIEWMODEL",
                                "No plants returned by the API (null body)."
                            )
                            tasksList = emptyList()  // Set to an empty list in case of null
                        }
                    } else {
                        // Handle non-success HTTP status codes (4xx, 5xx)
                        Log.e(
                            "TASKVIEWMODEL",
                            "API response error: ${response.code()} - ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<FirestoreTask>>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
    }

    fun deleteTask(userId: String, taskId: String) {
        firestoreApiInterface.deletePlant(plantId = taskId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        fetchTaskList(userId = userId)
                        Log.d("TASKVIEWMODEL", "Plant removed successfully")
                    } else {
                        Log.w(
                            "TASKVIEWMODEL",
                            "Error: ${response.code()} - ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
    }

}