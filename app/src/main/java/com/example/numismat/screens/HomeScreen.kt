package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.numismat.CoinsState
import com.example.numismat.components.CoinCard

// Аналог src/app/(tabs)/index.tsx.
// Раніше екран сам брав дані через `viewModel()`. Тепер отримує їх параметрами (як пропси):
// стан монет потрібен і тут, і на екрані монети, тому він живе вище — в RootLayout.
// Це і є state hoisting ("підняття стану"), як lifting state up у React.
// `(String) -> Unit` ≈ `(id: string) => void`.
@Composable
fun HomeScreen(state: CoinsState, onCoinClick: (String) -> Unit) {
    // Ранній return, як `if (loading || error) return <Text>...</Text>` в Expo.
    if (state.loading || state.error != null) {
        Text(state.error ?: "Завантаження...", modifier = Modifier.padding(16.dp))
        return
    }

    // LazyColumn ≈ <FlatList>: рендерить лише видимі елементи.
    // contentPadding ≈ contentContainerStyle={{ padding: 16 }}, spacedBy(12.dp) ≈ gap: 12.
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // items(data, key) { item -> ... } ≈ data + keyExtractor + renderItem.
        items(state.coins, key = { it.id }) { coin ->
            CoinCard(coin, onClick = { onCoinClick(coin.id) })
        }
    }
}
