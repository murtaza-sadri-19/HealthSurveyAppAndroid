package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser get() = auth.currentUser

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                fetchUserRole { role ->
                    val user = User(
                        id = currentUser?.uid ?: "",
                        email = email,
                        name = currentUser?.displayName ?: "",
                        role = role ?: "user"
                    )
                    _authState.value = AuthState(user = user)
                    onResult(true, null, user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
                onResult(false, e.message, null)
            }
        }
    }

    private fun fetchUserRole(onResult: (String?) -> Unit) {
        val uid = currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            try {
                val document = usersCollection.document(uid).get().await()
                val role = document.getString("role")
                println("Fetched role: $role") // Debug log
                onResult(role)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }
}
