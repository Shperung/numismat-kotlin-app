package com.example.numismat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numismat.model.Coin

// Аналог src/components/coin-details.tsx — повна інформація про монету.
// Використовується на екрані монети і на Головній (випадкова монета).
// `modifier: Modifier = Modifier` — домовленість у Compose: кожен компонент приймає modifier ззовні
// (≈ проп `style`), щоб батько міг додати відступи/розміри. `= Modifier` — порожній за замовчуванням.
@Composable
fun CoinDetails(coin: Coin, modifier: Modifier = Modifier) {
    // Column + verticalScroll ≈ <ScrollView contentContainerStyle={{ padding: 16, gap: 8 }}>.
    Column(
        modifier = modifier
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
        Text("Рік: ${coin.year}")
        Text("Країна: ${coin.country}")
        // ≈ `{coin.info ? <Text>...</Text> : null}`. У Compose просто `if` — без `: null`.
        if (!coin.info.isNullOrEmpty()) {
            Text(coin.info, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
        }
    }
}
