package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.numismat.lib.auth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Аналог src/components/login-form.tsx з веб-версії.
// Перехід в адмінку після входу не потрібен (у вебі — `redirect("/admin")`): AuthViewModel отримає
// нового користувача, і той самий таб покаже AdminScreen.
@Composable
fun LoginScreen() {
    // У вебі поля неконтрольовані (FormData). У Compose поле завжди контрольоване: value + onValueChange.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // ≈ `const [state, action, pending] = useActionState(login, null)`.
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun login() = scope.launch {
        pending = true
        error = null
        try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
        } catch (e: Exception) {
            // ≈ WRONG_CREDENTIALS.includes(message): тут помилки — окремі класи винятків.
            error = when (e) {
                is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
                    "Невірний email або пароль"
                else -> e.message ?: e.toString()
            }
        }
        pending = false
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Вхід в адмінку", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            // ≈ `type="email"`: клавіатура з @.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            // ≈ `type="password"`: крапки замість символів.
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        // `?.let` — показати, лише якщо є помилка (≈ `{state?.error && <p>...</p>}`).
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { login() },
            // ≈ `required` у вебі: без email і пароля кнопка неактивна.
            enabled = !pending && email.isNotBlank() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (pending) "Вхід..." else "Увійти")
        }
    }
}
