package com.example.healthsurveyappandroid.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.example.healthsurveyappandroid.R
import com.example.healthsurveyappandroid.data.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun registerUser(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _loginState.value = LoginState(
                isError = true,
                errorMessage = "Please fill all fields"
            )
            return
        }

        _loginState.value = LoginState(isLoading = true)
        viewModelScope.launch {
            try {
                // Create user with email and password
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: return@launch

                // Create user profile in Firestore
                val user = User(
                    id = userId,
                    email = email,
                    name = name,
                    role = "user" // Default role
                )

                // Store user data in Firestore
                usersCollection.document(userId).set(user).await()

                // Update login state
                _loginState.value = LoginState(
                    isSuccess = true,
                    user = user
                )
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    isError = true,
                    errorMessage = e.message ?: "Registration failed"
                )
            }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState(
                isError = true,
                errorMessage = "Please enter email and password"
            )
            return
        }

        _loginState.value = LoginState(isLoading = true)
        viewModelScope.launch {
            try {
                // Sign in with email and password
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: return@launch

                // Get user profile from Firestore
                val documentSnapshot = usersCollection.document(userId).get().await()

                if (documentSnapshot.exists()) {
                    // Convert document to User object
                    val user = documentSnapshot.toObject(User::class.java)
                        ?: User(id = userId, email = email)

                    // Update login state
                    _loginState.value = LoginState(
                        isSuccess = true,
                        user = user
                    )
                } else {
                    // Create a basic user profile if it doesn't exist
                    val user = User(
                        id = userId,
                        email = email,
                        name = email.substringBefore("@")
                    )

                    usersCollection.document(userId).set(user).await()
                    _loginState.value = LoginState(
                        isSuccess = true,
                        user = user
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    isError = true,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
        _loginState.value = LoginState()
    }

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _loginState.value = LoginState(isLoading = true)
            viewModelScope.launch {
                try {
                    val documentSnapshot = usersCollection.document(currentUser.uid).get().await()

                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = user
                        )
                    } else {
                        // Basic user profile if Firestore document doesn't exist
                        val email = currentUser.email ?: ""
                        val user = User(
                            id = currentUser.uid,
                            email = email,
                            name = email.substringBefore("@")
                        )

                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = user
                        )
                    }
                } catch (e: Exception) {
                    _loginState.value = LoginState()
                    auth.signOut() // Sign out on error
                }
            }
        }
    }

    // In AuthViewModel.kt
    fun signInWithGoogle(activity: ComponentActivity) {
        _loginState.value = LoginState(isLoading = true)

        // Create Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Launch credential manager
        val credentialManager = CredentialManager.create(activity)
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val credential = GoogleAuthProvider.getCredential(
                    (result.credential as GetGoogleIdCredentialResponse).googleIdToken, null
                )

                // Sign in with Firebase
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    _loginState.value = LoginState(
                        isSuccess = true,
                        user = User(
                            id = user.uid,
                            email = user.email ?: "",
                            name = user.displayName ?: "",
                            role = "user" // Default role
                        )
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    isError = true,
                    errorMessage = e.message ?: "Google Sign-in failed"
                )
            }
        }
    }

}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val user: User? = null
)
