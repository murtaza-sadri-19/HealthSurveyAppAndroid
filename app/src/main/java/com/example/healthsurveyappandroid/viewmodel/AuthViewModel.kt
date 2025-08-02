package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.data.User
import com.google.firebase.auth.AuthCredential
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

    /**
     * Attempts to log in the user with email and password.
     * Updates authState accordingly and triggers onResult callback.
     */
    fun loginUser(
        email: String,
        password: String,
        onResult: (success: Boolean, errorMessage: String?, user: User?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            runCatching {
                auth.signInWithEmailAndPassword(email, password).await()
            }.onSuccess {
                // After successful sign-in, fetch user role from Firestore
                fetchUserRole { role ->
                    val user = User(
                        id = currentUser?.uid ?: "",
                        email = email,
                        name = currentUser?.displayName ?: "",
                        role = role ?: "user"
                    )
                    _authState.value = AuthState(user = user, isLoading = false)
                    onResult(true, null, user)
                }
            }.onFailure { exception ->
                // On failure, set error in state and notify callback
                val errorMsg = exception.message ?: "Login failed"
                _authState.value = AuthState(error = errorMsg, isLoading = false)
                onResult(false, errorMsg, null)
            }
        }
    }

    /**
     * Fetch the role string of the current logged-in user from Firestore.
     * Calls onResult with role string or null if not found or on error.
     */
    private fun fetchUserRole(onResult: (String?) -> Unit) {
        val uid = currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            runCatching {
                usersCollection.document(uid).get().await()
            }.onSuccess { document ->
                val role = document.getString("role")
                onResult(role)
            }.onFailure { exception ->
                exception.printStackTrace()
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Sign out the current user and reset auth state.
     */
    // ✅ NEW FUNCTION to support Compose-style Google Sign-In
    fun signInWithGoogleCredential(
        credential: AuthCredential,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            try {
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user ?: throw Exception("Firebase user is null")

                val uid = firebaseUser.uid
                val userDoc = usersCollection.document(uid).get().await()

                // If new user, save to Firestore
                if (!userDoc.exists()) {
                    usersCollection.document(uid).set(
                        mapOf(
                            "email" to firebaseUser.email,
                            "name" to firebaseUser.displayName,
                            "role" to "user" // default role
                        )
                    ).await()
                }

                // Fetch role (or fallback to user)
                val role = userDoc.getString("role") ?: "user"

                val user = User(
                    id = uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    role = role
                )

                _authState.value = AuthState(user = user)
                onResult(true, null)

            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
                onResult(false, e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }

    /**
     * Clears the current authentication error from state.
     */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}