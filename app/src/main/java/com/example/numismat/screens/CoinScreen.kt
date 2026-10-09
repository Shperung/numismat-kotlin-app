package com.example.numismat.screens

import androidx.compose.foundation.layout.padding
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
import com.example.numismat.components.CoinDetails
import com.example.numismat.model.Coin

// Аналог src/app/coin/[id].tsx.
// В Expo екран сам дістає id (`useLocalSearchParams`) і шукає монету в `useCoins()`.
// Тут RootLayout вже знайшов монету і передав її готовою — екран лише малює.
// `coin: Coin?` — може бути null, якщо монету не знайдено.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinScreen(coin: Coin?, onBack: () -> Unit, onEdit: ((String) -> Unit)?) {
    // Stack в Expo сам малює хедер зі стрілкою "Назад" (options: { title: 'Монета' }).
    // У Compose хедер збираємо самі: Scaffold + TopAppBar + кнопка-стрілка.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Монета") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // AutoMirrored — стрілка сама розвернеться для RTL-мов (арабська, іврит).
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (coin == null) {
            Text("Монету не знайдено", modifier = Modifier.padding(innerPadding).padding(16.dp))
        } else {
            // Тут Kotlin уже "знає", що coin не null (smart cast), тому можна передати його як `Coin`.
            // padding(innerPadding) — щоб контент не залазив під хедер.
            CoinDetails(coin, modifier = Modifier.padding(innerPadding), onEdit = onEdit)
        }
    }
}
