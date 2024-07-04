package com.example.whatstheplant.signin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GoogleSignInViewModel: ViewModel() {
    private val _state = MutableStateFlow(GoogleSignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: GoogleSignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )}
    }

    fun resetState(){
        _state.update { GoogleSignInState() }
    }
}