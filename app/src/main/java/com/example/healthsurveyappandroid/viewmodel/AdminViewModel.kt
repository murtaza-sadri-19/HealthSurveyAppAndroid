// viewmodel/AdminViewModel.kt
package com.example.healthsurveyappandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.healthsurveyappandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewModel : ViewModel() {
    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> get() = _users

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUsers()  // Fixed missing closing brace
    }

    private fun loadUsers() {
        firestore.collection("users")
            .addSnapshotListener { snapshots, _ ->
                _users.value = snapshots?.toObjects(User::class.java) ?: emptyList()
            }
    }

    fun createUser(name: String, email: String, password: String, role: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = User(
                    id = authResult.user?.uid ?: "",
                    name = name,
                    email = email,
                    role = role
                )
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun deleteUser(userId: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        firestore.collection("users").document(userId)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun updateUserRole(userId: String, newRole: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(userId)
            .update("role", newRole)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}