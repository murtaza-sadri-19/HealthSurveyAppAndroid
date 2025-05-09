package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsurveyappandroid.viewmodel.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.example.healthsurveyappandroid.ui.components.User



class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun registerUser(email: String, password: String, name: String) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            _loginState.value = LoginState(
                isError = true,
                errorMessage = "Please fill all fields"
            )
            return
        }

        _loginState.value = LoginState(isLoading = true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "role" to "user"
                )

                usersCollection.document(authResult.user?.uid ?: "")
                    .set(user)
                    .addOnSuccessListener {
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = User(
                                id = authResult.user?.uid ?: "",
                                email = email,
                                name = name
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        _loginState.value = LoginState(
                            isError = true,
                            errorMessage = "Registration failed: ${e.message}"
                        )
                    }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState(
                    isError = true,
                    errorMessage = "Registration failed: ${e.message}"
                )
            }
    }

    fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState(
                isError = true,
                errorMessage = "Please fill all fields"
            )
            return
        }

        _loginState.value = LoginState(isLoading = true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                usersCollection.document(authResult.user?.uid ?: "")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userData = document.data
                            _loginState.value = LoginState(
                                isSuccess = true,
                                user = User(
                                    id = authResult.user?.uid ?: "",
                                    email = userData?.get("email") as? String ?: "",
                                    name = userData?.get("name") as? String ?: "",
                                    role = userData?.get("role") as? String ?: "user"
                                )
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        _loginState.value = LoginState(
                            isError = true,
                            errorMessage = "Login failed: ${e.message}"
                        )
                    }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState(
                    isError = true,
                    errorMessage = "Authentication failed"
                )
            }
    }

    fun logout() {
        auth.signOut()
        _loginState.value = LoginState()
    }

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersCollection.document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.data
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = User(
                                id = currentUser.uid,
                                email = userData?.get("email") as? String ?: "",
                                name = userData?.get("name") as? String ?: "",
                                role = userData?.get("role") as? String ?: "user"
                            )
                        )
                    }
                }
        }
    }
}