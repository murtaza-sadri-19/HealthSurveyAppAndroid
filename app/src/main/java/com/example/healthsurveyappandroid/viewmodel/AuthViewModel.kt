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

enum class NavigationState { AUTH, USER, ADMIN }

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

    // Public registration is disabled; only admin can create users.
    // The registerUser function can be kept for admin use only, or removed from UI.

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                fetchUserRole { role ->
                    val user = User(
                        id = currentUser?.uid ?: "",
                        email = email,
                        name = currentUser?.displayName ?: "",
                        role = role
                    )
                    _authState.value = AuthState(user = user)
                    onResult(true, null)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
                onResult(false, e.message)
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