package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numismat.components.CoinPhoto
import com.example.numismat.model.Coin

// Аналог src/app/coin/[id].tsx.
// В Expo екран сам дістає id (`useLocalSearchParams`) і шукає монету в `useCoins()`.
// Тут RootLayout вже знайшов монету і передав її готовою — екран лише малює.
// `coin: Coin?` — може бути null, якщо монету не знайдено.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinScreen(coin: Coin?, onBack: () -> Unit) {
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
            // Column + verticalScroll ≈ <ScrollView contentContainerStyle={{ padding: 16, gap: 8 }}>.
            // Тут Kotlin уже "знає", що coin не null (smart cast), тому пишемо `coin.name` без `?.`.
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // spacedBy(12.dp, Alignment.CenterHorizontally) ≈ gap: 12 + justifyContent: 'center'.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    CoinPhoto(coin.avers, 150.dp)
                    CoinPhoto(coin.revers, 150.dp)
                }
                Text(coin.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${coin.value} ${coin.currency}")
                Text("Країна: ${coin.country}")
                // ≈ `{coin.info ? <Text>...</Text> : null}`. У Compose просто `if` — без `: null`.
                if (!coin.info.isNullOrEmpty()) {
                    Text(coin.info, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
                }
            }
        }
    }
}
