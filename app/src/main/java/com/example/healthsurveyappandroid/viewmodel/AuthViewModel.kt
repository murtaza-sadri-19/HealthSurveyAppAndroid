package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.viewmodel.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(1000)
            if (username == "admin") {
                _loginState.value = LoginState.Success("admin")
            } else if (username == "user") {
                _loginState.value = LoginState.Success("user")
            } else {
                _loginState.value = LoginState.Error("Invalid credentials")
            }
        }
    }
}
