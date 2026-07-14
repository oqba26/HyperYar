package com.oqba26.hyperyar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.oqba26.hyperyar.util.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val passwordFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = if (isSignUp) "ساخت حساب فروشگاه" else "ورود به هایپریار",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = emailValue,
                onValueChange = { 
                    emailValue = it
                    val isCommonTld = it.endsWith(".com") || it.endsWith(".ir") || it.endsWith(".net") || it.endsWith(".org")
                    val hasLongTld = it.substringAfterLast(".", "").length >= 3
                    
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() && (isCommonTld || hasLongTld)) {
                        passwordFocusRequester.requestFocus()
                    }
                },
                label = { Text("ایمیل") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                )
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = { Text("رمز عبور") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                )
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
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val client = SupabaseManager.getClient()
                        if (client == null) {
                            errorMessage = "تنظیمات اتصال برقرار نیست"
                            isLoading = false
                            return@launch
                        }
                        
                        try {
                            if (isSignUp) {
                                client.auth.signUpWith(Email) {
                                    email = emailValue
                                    password = passwordValue
                                }
                            } else {
                                client.auth.signInWith(Email) {
                                    email = emailValue
                                    password = passwordValue
                                }
                            }
                            onLoginSuccess()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val url = client.supabaseUrl
                            errorMessage = when {
                                e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> 
                                    "خطا در اتصال: آدرس سرور پیدا نشد. لطفاً اینترنت و آدرس وارد شده را چک کنید.\nURL: $url"
                                e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> "ایمیل یا رمز عبور اشتباه است"
                                else -> "خطایی رخ داد: ${e.javaClass.simpleName} - ${e.message ?: "نامشخص"}"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && emailValue.isNotBlank() && passwordValue.length >= 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(if (isSignUp) "ثبت‌نام" else "ورود")
                }
            }

            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(if (isSignUp) "قبلاً حساب ساخته‌اید؟ ورود" else "حساب ندارید؟ ثبت‌نام فروشگاه")
            }
        }
    }
}
