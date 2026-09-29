package com.example.numismat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.numismat.model.Coin

// Аналог src/components/coin-card.tsx.
// В Expo картка сама знає, куди вести (<Link href={`/coin/${coin.id}`}>).
// У Compose прийнято інакше: компонент лише повідомляє "мене натиснули" через колбек `onClick`,
// а куди переходити — вирішує той, хто знає про навігацію (RootLayout). Як проп `onPress` у RN.
// `() -> Unit` ≈ `() => void` у TS.
@Composable
fun CoinCard(coin: Coin, onClick: () -> Unit) {
    // Card з onClick = <Pressable style={styles.card}>: фон, заокруглення і ripple-ефект вже з теми.
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        // Row ≈ <View style={{ flexDirection: 'row' }}>; spacedBy(12.dp) ≈ gap: 12.
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CoinPhoto(coin.avers, 56.dp)
                CoinPhoto(coin.revers, 56.dp)
            }
            // Column ≈ <View> з flexDirection: 'column' (у RN це за замовчуванням, у Compose — окремий компонент).
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(coin.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                // "${...}" — шаблонний рядок, як `${coin.value} ${coin.currency}` у TS.
                Text("${coin.value} ${coin.currency}")
                // Замість '#666' беремо "приглушений" колір з теми — він підлаштується під темну тему.
                Text(coin.country, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Кругле фото монети — використовується і в картці, і на екрані монети.
// В Expo це <Image style={{ width, height, borderRadius: size / 2 }} contentFit="cover" />.
// `url: String?` — фото може не бути; тоді AsyncImage просто нічого не намалює, лишиться сірий фон.
@Composable
fun CoinPhoto(url: String?, size: Dp) {
    AsyncImage(
        model = url,
        // Опис для screen reader (≈ accessibilityLabel). null — картинка декоративна.
        contentDescription = null,
        // ContentScale.Crop ≈ contentFit="cover".
        contentScale = ContentScale.Crop,
        // Порядок модифікаторів важливий: спочатку розмір, потім обрізати по колу, потім фон.
        // clip(CircleShape) ≈ borderRadius: size / 2 + overflow: 'hidden'.
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEEEEEE)),
    )
}
