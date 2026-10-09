package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.numismat.components.CoinForm
import com.example.numismat.lib.auth
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import com.google.firebase.auth.FirebaseUser

// Аналог src/app/admin/page.tsx: "хто увійшов" + "Вийти" + форма нової монети. Форма країни — наступний крок.
@Composable
fun AdminScreen(user: FirebaseUser, countries: List<Country>, onCoinAdded: (Coin) -> Unit) {
    // fillMaxSize — інакше Pager центрує невисокий контент посередині екрана.
    // imePadding — відступ під клавіатуру, щоб нижні поля можна було докрутити (≈ KeyboardAvoidingView).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(user.email ?: "")
            // ≈ `<form action={logout}>`: signOut → AuthViewModel отримає null → таб знову покаже LoginScreen.
            TextButton(onClick = { auth.signOut() }) { Text("Вийти") }
        }
        CoinForm(countries, onSaved = onCoinAdded)
    }
}
