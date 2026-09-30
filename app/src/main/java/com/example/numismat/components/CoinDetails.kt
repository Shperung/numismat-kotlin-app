package com.example.numismat.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numismat.lib.startCoinChat
import com.example.numismat.model.Coin
import kotlinx.coroutines.launch

// Аналог src/components/coin-details.tsx — повна інформація про монету.
// Використовується на екрані монети і на Головній (випадкова монета).
// `modifier: Modifier = Modifier` — домовленість у Compose: кожен компонент приймає modifier ззовні
// (≈ проп `style`), щоб батько міг додати відступи/розміри. `= Modifier` — порожній за замовчуванням.
@Composable
fun CoinDetails(coin: Coin, modifier: Modifier = Modifier) {
    // ≈ `const [answer, setAnswer] = useState<string | null>(null)` і `const [loading, setLoading] = useState(false)`.
    var answer by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    // Запит до AI — suspend-функція, тож потрібна корутина. Цей scope скасує запит, якщо компонент зникне з екрана.
    val scope = rememberCoroutineScope()

    // ≈ `const askFacts = async () => {...}`: `scope.launch { }` — як виклик async-функції з onPress.
    fun askFacts() {
        scope.launch {
            loading = true
            try {
                // `sendMessage` ≈ `await chat.sendMessage(...)`; `.text` ≈ `result.response.text()`.
                answer = startCoinChat(coin).sendMessage("Розкажи цікаві факти про цю монету").text
            } catch (e: Exception) {
                // Log.e ≈ console.error: повна помилка зі стеком — у вкладці Logcat в Android Studio (фільтр "AI").
                Log.e("AI", "askFacts", e)
                // SDK часто загортає справжню причину (HTTP-помилку тощо) у загальний виняток — вона в `e.cause`.
                answer = "Помилка: $e\nПричина: ${e.cause}"
            } finally {
                loading = false
            }
        }
    }

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

        // Button ≈ <Pressable style={styles.aiButton} disabled={loading}>: колір і заокруглення беруться з теми.
        // Вміст кнопки — це вже Row, тож іконка й текст стають в рядок самі.
        Button(
            onClick = { askFacts() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            // Іконки "sparkles" немає в базовому наборі Material Icons, тому зірка.
            Icon(Icons.Filled.Star, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Дізнатись цікаві факти про цю монету")
        }
        // CircularProgressIndicator ≈ <ActivityIndicator />. align — центрувати лише цей елемент у Column.
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        // Локальна копія — для smart cast, як на Головній (`answer` теж делегат через `by`).
        val text = answer
        if (text != null) {
            Text(text, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
        }
    }
}
