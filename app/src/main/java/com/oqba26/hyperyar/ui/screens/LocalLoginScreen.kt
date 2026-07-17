package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.oqba26.hyperyar.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalLoginScreen(
    users: List<User>,
    isSyncing: Boolean = false,
    onLoginSuccess: (User) -> Unit
) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isSyncing && users.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("در حال بارگذاری لیست کاربران...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "ورود به سیستم فروش",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (selectedUser == null) {
                Text(
                    text = "لطفاً کاربر خود را انتخاب کنید:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    LazyColumn {
                        items(users) { user ->
                            ListItem(
                                headlineContent = { Text(user.username) },
                                supportingContent = { Text(if (user.role == "ADMIN") "مدیر" else "صندوق‌دار") },
                                leadingContent = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.clickable { 
                                    selectedUser = user
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "کاربر: ${selectedUser?.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    label = { Text("رمز عبور") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedUser?.passwordHash == password.trim()) {
                            onLoginSuccess(selectedUser!!)
                        } else {
                            errorMessage = "رمز عبور اشتباه است"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("ورود")
                }

                TextButton(onClick = { 
                    selectedUser = null
                    password = ""
                }) {
                    Text("تغییر کاربر")
                }
            }
            }
        }
    }
}
