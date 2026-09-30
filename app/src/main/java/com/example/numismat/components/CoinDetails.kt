package com.example.numismat.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numismat.R
import com.example.numismat.lib.Message
import com.example.numismat.lib.askServer
import com.example.numismat.lib.startCoinChat
import com.example.numismat.model.Coin
import kotlinx.coroutines.launch

// `const` — константа часу компіляції (≈ `const QUESTION = '...'` на рівні модуля).
private const val QUESTION = "Розкажи цікаві факти про цю монету"

// ≈ елемент масиву aiButtons: `{ id, title, logo, ask }`.
// `@DrawableRes Int` — картинка з res/drawable передається як числовий id (≈ `require('.../groq.png')`).
// `suspend (Coin) -> String?` ≈ `(coin: Coin) => Promise<string>`: suspend-функція як значення поля.
private data class AiButton(
    val id: String,
    val title: String,
    @DrawableRes val logo: Int,
    val ask: suspend (Coin) -> String?,
)

// Лямбда після дужок `AiButton(...) { coin -> ... }` — це останній параметр `ask` (trailing lambda).
private val aiButtons = listOf(
    AiButton("gemini", "Запитати в Gemini про монету", R.drawable.ai_gemini) { coin ->
        // `sendMessage` ≈ `await chat.sendMessage(...)`; `.text` ≈ `result.response.text()`.
        startCoinChat(coin).sendMessage(QUESTION).text
    },
    AiButton("groq", "Запитати в Groq про монету", R.drawable.ai_groq) { coin ->
        askServer("groq-gpt-oss", coin, listOf(Message("user", QUESTION)))
    },
)

// Аналог src/components/coin-details.tsx — повна інформація про монету.
// Використовується на екрані монети і на Головній (випадкова монета).
// `modifier: Modifier = Modifier` — домовленість у Compose: кожен компонент приймає modifier ззовні
// (≈ проп `style`), щоб батько міг додати відступи/розміри. `= Modifier` — порожній за замовчуванням.
@Composable
fun CoinDetails(coin: Coin, modifier: Modifier = Modifier) {
    // ≈ `const [answer, setAnswer] = useState<string | null>(null)`.
    var answer by remember { mutableStateOf<String?>(null) }
    // ≈ `const [loadingId, setLoadingId] = useState<string | null>(null)` — яка кнопка зараз чекає відповідь.
    var loadingId by remember { mutableStateOf<String?>(null) }
    // Запит до AI — suspend-функція, тож потрібна корутина. Цей scope скасує запит, якщо компонент зникне з екрана.
    val scope = rememberCoroutineScope()

    // ≈ `const askFacts = async (button) => {...}`: `scope.launch { }` — як виклик async-функції з onPress.
    fun askFacts(button: AiButton) {
        scope.launch {
            loadingId = button.id
            try {
                answer = button.ask(coin)
            } catch (e: Exception) {
                // Log.e ≈ console.error: повна помилка зі стеком — у вкладці Logcat в Android Studio (фільтр "AI").
                Log.e("AI", "askFacts", e)
                // SDK часто загортає справжню причину (HTTP-помилку тощо) у загальний виняток — вона в `e.cause`.
                answer = "Помилка: $e\nПричина: ${e.cause}"
            } finally {
                loadingId = null
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

        // ≈ <View style={styles.aiButtons}> + aiButtons.map(...).
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            aiButtons.forEach { button ->
                // OutlinedButton ≈ <Pressable> з білим фоном і рамкою (styles.aiButton).
                // Вміст кнопки — це вже Row, тож лого, текст і спінер стають в рядок самі.
                OutlinedButton(
                    onClick = { askFacts(button) },
                    // ≈ disabled={loadingId !== null}: поки чекаємо одну відповідь, обидві кнопки неактивні.
                    enabled = loadingId == null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Image + painterResource ≈ <Image source={require(...)} />.
                    Image(
                        painter = painterResource(button.logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.width(10.dp))
                    // weight(1f) ≈ flex: 1 — текст займає все вільне місце, спінер притискається вправо.
                    Text(button.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    // ≈ {loadingId === button.id ? <ActivityIndicator /> : null}.
                    if (loadingId == button.id) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
        // Локальна копія — для smart cast, як на Головній (`answer` теж делегат через `by`).
        val text = answer
        if (text != null) {
            Text(text, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
        }
    }
}
