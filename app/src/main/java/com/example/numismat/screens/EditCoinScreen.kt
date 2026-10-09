package com.example.numismat.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.numismat.components.CoinForm
import com.example.numismat.model.Coin
import com.example.numismat.model.Country

// Аналог src/app/admin/edit/[id]/page.tsx: та сама форма, заповнена монетою.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCoinScreen(coin: Coin?, countries: List<Country>, onBack: () -> Unit, onSaved: (Coin) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редагування") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (coin == null) {
            Text("Монету не знайдено", modifier = Modifier.padding(innerPadding).padding(16.dp))
        } else {
            CoinForm(
                countries = countries,
                editing = coin,
                onSaved = onSaved,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}
