package com.example.whatstheplant.signin

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel: ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val _userData = MutableStateFlow(SignInResult(data=null, errorMessage = null))
    val userData = _userData.asStateFlow()

    fun onSignInResult(result: SignInResult, context: Context) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )}
        _userData.value = SignInResult(data = result.data, errorMessage = result.errorMessage)
        Toast.makeText(
            context,
            "Success",
            Toast.LENGTH_LONG
        ).show()
    }

    fun resetState(){
        _state.update { SignInState() }
    }
}