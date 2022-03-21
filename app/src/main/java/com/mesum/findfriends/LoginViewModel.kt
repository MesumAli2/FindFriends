package com.mesum.findfriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class LoginViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }


    val authenticationState = FirebaseUserLiveData().map {
        if (it != null){
            AuthenticationState.AUTHENTICATED
        }
        else{
            AuthenticationState.UNAUTHENTICATED

        }
    }
}
