package com.example.numismat.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.numismat.components.CoinForm
import com.example.numismat.lib.deleteCoin
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import kotlinx.coroutines.launch

// Аналог src/app/admin/edit/[id]/page.tsx: та сама форма, заповнена монетою.
// Плюс видалення, як в Expo (src/app/admin/coin.tsx): кнопка-кошик у хедері → підтвердження.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCoinScreen(
    coin: Coin?,
    countries: List<Country>,
    onBack: () -> Unit,
    onSaved: (Coin) -> Unit,
    onDeleted: (String) -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редагування") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                // actions — іконки праворуч у хедері (≈ `headerRight` у React Navigation).
                actions = {
                    if (coin != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Видалити монету")
                        }
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

    // ≈ `Alert.alert('Видалити монету?', name, [Скасувати, Видалити])` в Expo.
    // AlertDialog — звичайний composable: показується, поки `confirmDelete == true`.
    if (confirmDelete && coin != null) {
        AlertDialog(
            onDismissRequest = {
                if (!deleting) {
                    confirmDelete = false
                    deleteError = null
                }
            },
            title = { Text("Видалити монету?") },
            text = { Text(deleteError ?: coin.name) },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        scope.launch {
                            deleting = true
                            deleteError = null
                            try {
                                deleteCoin(coin)
                                onDeleted(coin.id)
                            } catch (e: Exception) {
                                deleteError = e.message ?: e.toString()
                            }
                            deleting = false
                        }
                    },
                ) { Text(if (deleting) "Видалення..." else "Видалити", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(enabled = !deleting, onClick = { confirmDelete = false; deleteError = null }) {
                    Text("Скасувати")
                }
            },
        )
    }
}
