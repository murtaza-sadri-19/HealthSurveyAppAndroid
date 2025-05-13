package com.example.healthsurveyappandroid.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import com.example.healthsurveyappandroid.data.User
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState  // Ensure User has email/role/id fields
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthsurveyappandroid.viewmodel.AdminViewModel

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var email by remember { mutableStateOf(user.email) }
    var role by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit User") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (admin/user)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(user.copy(email = email, role = role))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ManageUsersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.observeAsState(initial = emptyList())
    var editingUser by remember { mutableStateOf<User?>(null) }

    LazyColumn {
        items(users) { user ->
            Card {
                Column {
                    Text("Email: ${user.email}")
                    Text("Role: ${user.role}")
                    Row {
                        Button(onClick = { editingUser = user }) {
                            Text("Edit")
                        }
                        Button(onClick = { viewModel.deleteUser(user.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    // Show dialog if a user is being edited
    editingUser?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { editingUser = null },
            onSave = { updatedUser ->
                // Only update the role in Firestore to avoid email update issues
                viewModel.updateUserRole(updatedUser.id, updatedUser.role) { success, error ->
                    // Optionally show a message
                }
                editingUser = null
            }
        )
    }
}
