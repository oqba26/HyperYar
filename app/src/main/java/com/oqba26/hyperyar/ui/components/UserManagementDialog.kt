package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.oqba26.hyperyar.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementDialog(
    users: List<User>,
    onDismiss: () -> Unit,
    onAddUser: (String, String, String) -> Unit,
    onUpdateUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit
) {
    var showAddUserForm by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                topBar = {
                    MediumTopAppBar(
                        title = { Text("مدیریت کاربران", style = MaterialTheme.typography.titleLarge) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "بستن")
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = { showAddUserForm = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("افزودن کاربر")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                floatingActionButton = { }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (user.role == "ADMIN") "مدیر سیستم" else "صندوق‌دار",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (user.role == "ADMIN") MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { 
                                        editingUser = user
                                        username = user.username
                                        password = user.passwordHash
                                        role = user.role
                                        showAddUserForm = true 
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    
                                    val canDelete = user.username != "admin" || users.count { it.role == "ADMIN" } > 1
                                    if (canDelete) {
                                        IconButton(onClick = { userToDelete = user }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (userToDelete != null) {
                    ConfirmDialog(
                        title = "حذف کاربر",
                        message = "آیا از حذف کاربر «${userToDelete?.username}» اطمینان دارید؟ این عمل غیرقابل بازگشت است.",
                        confirmText = "حذف",
                        cancelText = "انصراف",
                        isDanger = true,
                        onConfirm = {
                            onDeleteUser(userToDelete!!)
                            userToDelete = null
                        },
                        onDismiss = { userToDelete = null }
                    )
                }

                if (showAddUserForm) {
                    HyperDialog(
                        onDismissRequest = {
                            showAddUserForm = false
                            editingUser = null
                            username = ""
                            password = ""
                            role = "STAFF"
                        },
                        title = if (editingUser != null) "ویرایش کاربر" else "افزودن کاربر جدید",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("نام کاربری") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("رمز عبور") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("نقش کاربر:", style = MaterialTheme.typography.labelLarge)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = role == "STAFF",
                                            onClick = { role = "STAFF" }
                                        )
                                        Text("صندوق‌دار")
                                        Spacer(Modifier.width(16.dp))
                                        RadioButton(
                                            selected = role == "ADMIN",
                                            onClick = { role = "ADMIN" }
                                        )
                                        Text("مدیر")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (username.isNotBlank() && password.isNotBlank()) {
                                        val cleanUsername = username.trim().lowercase()
                                        val cleanPassword = password.trim()
                                        if (editingUser != null) {
                                            onUpdateUser(editingUser!!.copy(username = cleanUsername, passwordHash = cleanPassword, role = role))
                                        } else {
                                            onAddUser(cleanUsername, cleanPassword, role)
                                        }
                                        username = ""
                                        password = ""
                                        role = "STAFF"
                                        editingUser = null
                                        showAddUserForm = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("تایید", style = MaterialTheme.typography.labelLarge)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showAddUserForm = false
                                    editingUser = null
                                    username = ""
                                    password = ""
                                    role = "STAFF"
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("انصراف", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    )
                }
            }
        }
    }
}
}
