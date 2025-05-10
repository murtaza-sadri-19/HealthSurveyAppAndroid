package com.example.healthsurveyappandroid.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Navigation destinations
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

    fun registerUser(email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("User creation failed")

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)?.await()

                val role = if (email == "admin@email.com") "admin" else "user"
                val user = User(id = userId, email = email, name = name, role = role)
                usersCollection.document(userId).set(user).await()

                _authState.value = AuthState(user = user)
                onResult(true, null)
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
                onResult(false, e.message)
            }
        }
    }

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
                        role = role ?: "user"
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
        currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                try {
                    val document = usersCollection.document(uid).get().await()
                    onResult(document.getString("role"))
                } catch (e: Exception) {
                    onResult(null)
                }
            }
        } ?: onResult(null)
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }
}
