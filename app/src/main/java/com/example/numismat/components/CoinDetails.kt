package com.example.numismat.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.numismat.R
import com.example.numismat.lib.Message
import com.example.numismat.lib.Provider
import com.example.numismat.lib.askServer
import com.example.numismat.lib.fetchProviders
import com.example.numismat.lib.startCoinChat
import com.example.numismat.model.Coin
import kotlinx.coroutines.launch

// `const` — константа часу компіляції (≈ `const QUESTION = '...'` на рівні модуля).
private const val QUESTION = "Розкажи цікаві факти про цю монету"

// ≈ `type AiButton = { id; title; logo: number | string; ask }`.
// `logo: Any` ≈ `number | string`: або id картинки з res/drawable (R.drawable...), або URL з сервера.
// Coil `AsyncImage` приймає обидва варіанти — як `expo-image` приймає і `require(...)`, і рядок-URL.
// `suspend (Coin) -> String?` ≈ `(coin: Coin) => Promise<string>`: suspend-функція як значення поля.
private data class AiButton(
    val id: String,
    val title: String,
    val logo: Any,
    val ask: suspend (Coin) -> String?,
)

// ≈ `answers: Record<string, { text: string; error?: boolean }>` — одна відповідь.
private data class Answer(val text: String, val error: Boolean = false)

// Лямбда після дужок `AiButton(...) { coin -> ... }` — це останній параметр `ask` (trailing lambda).
private val geminiButton = AiButton("gemini", "Запитати в Gemini про монету", R.drawable.ai_gemini) { coin ->
    // `sendMessage` ≈ `await chat.sendMessage(...)`; `.text` ≈ `result.response.text()`.
    startCoinChat(coin).sendMessage(QUESTION).text
}

// ≈ `const toButton = (p: Provider): AiButton => ({ ... })`.
private fun toButton(p: Provider) = AiButton(p.id, "Запитати в ${p.title} про монету", p.logo) { coin ->
    askServer(p.id, coin, listOf(Message("user", QUESTION)))
}

// Аналог src/components/coin-details.tsx — повна інформація про монету.
// Використовується на екрані монети і на Головній (випадкова монета).
// `modifier: Modifier = Modifier` — домовленість у Compose: кожен компонент приймає modifier ззовні
// (≈ проп `style`), щоб батько міг додати відступи/розміри. `= Modifier` — порожній за замовчуванням.
@Composable
fun CoinDetails(coin: Coin, modifier: Modifier = Modifier) {
    // В Expo — Record<id, ...>. Тут:
    //   answers — Map<id, Answer> (≈ Record),
    //   open / loading — Set<id>: "id є в множині" ≈ `open[id] === true`.
    // Map і Set тут незмінні: щоб оновити, присвоюємо нову копію (`answers + (id to ...)` ≈ `{ ...a, [id]: ... }`),
    // і Compose, як React після setState, перемальовує екран.
    var answers by remember { mutableStateOf(mapOf<String, Answer>()) }
    var open by remember { mutableStateOf(setOf<String>()) }
    var loading by remember { mutableStateOf(setOf<String>()) }
    var providers by remember { mutableStateOf(listOf<Provider>()) }
    var providersError by remember { mutableStateOf<String?>(null) }
    // URL відкритого на весь екран фото; null — переглядач закритий.
    var photo by remember { mutableStateOf<String?>(null) }
    // Запит до AI — suspend-функція, тож потрібна корутина. Цей scope скасує запити, якщо компонент зникне з екрана.
    val scope = rememberCoroutineScope()

    // ≈ useEffect(() => { fetchProviders().then(...).catch(...) }, []).
    // `LaunchedEffect(Unit)` — ключ ніколи не змінюється, тож блок виконається один раз при появі на екрані.
    LaunchedEffect(Unit) {
        try {
            providers = fetchProviders()
        } catch (e: Exception) {
            providersError = e.toString()
        }
    }

    // ≈ `[geminiButton, ...providers.map(toButton)]`.
    val aiButtons = listOf(geminiButton) + providers.map { toButton(it) }

    fun onPress(button: AiButton) {
        val id = button.id
        // Вже є успішна відповідь — лише показати/сховати (≈ `setOpen((o) => ({ ...o, [id]: !o[id] }))`).
        if (answers[id]?.error == false) {
            open = if (id in open) open - id else open + id
            return
        }
        loading = loading + id
        open = open + id
        scope.launch {
            // Спершу чекаємо текст, і лише потім читаємо `answers`: за час запиту могли прийти відповіді
            // від інших кнопок, і треба доповнити свіжу Map, а не ту, що була до запиту
            // (у React для цього — функціональний `setAnswers((a) => ...)`).
            val answer = try {
                Answer(button.ask(coin).orEmpty())
            } catch (e: Exception) {
                // Log.e ≈ console.error: повна помилка зі стеком — у вкладці Logcat в Android Studio (фільтр "AI").
                Log.e("AI", "onPress ${button.id}", e)
                // SDK часто загортає справжню причину (HTTP-помилку тощо) у загальний виняток — вона в `e.cause`.
                Answer("Помилка: $e\nПричина: ${e.cause}", error = true)
            }
            answers = answers + (id to answer)
            loading = loading - id
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
            // ≈ [coin.avers, coin.revers].map((uri) => <Link href={{ pathname: '/photo', ... }} disabled={!uri}>).
            listOf(coin.avers, coin.revers).forEach { uri ->
                CoinPhoto(uri, 150.dp, Modifier.clickable(enabled = uri != null) { photo = uri })
            }
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
                AiItem(
                    button = button,
                    answer = answers[button.id],
                    isOpen = button.id in open,
                    isLoading = button.id in loading,
                    onClick = { onPress(button) },
                )
            }
            // Локальна копія — для smart cast (`providersError` — делегат через `by`).
            val error = providersError
            if (error != null) {
                Text("Помилка завантаження моделей: $error", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // ≈ <Modal visible={photo != null}>: поки photo не null — показуємо переглядач поверх екрана.
    val openPhoto = photo
    if (openPhoto != null) {
        PhotoViewer(openPhoto, onDismiss = { photo = null })
    }
}

// Один пункт акордеона: рядок-кнопка + відповідь під нею (≈ <View style={styles.aiItem}>).
@Composable
private fun AiItem(button: AiButton, answer: Answer?, isOpen: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    // OutlinedCard ≈ View з рамкою, заокругленням і overflow: 'hidden'. Сама картка не клікабельна —
    // інакше тап по тексту відповіді теж ховав би її. Клікабельний лише верхній рядок.
    OutlinedCard(shape = RoundedCornerShape(12.dp)) {
        // Row + clickable ≈ <Pressable style={styles.aiButton} disabled={loading[id]}>.
        // Порядок: спершу clickable, потім padding — щоб ripple-ефект покривав увесь рядок разом з відступами.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading, onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AsyncImage(
                model = button.logo,
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
            )
            // weight(1f) ≈ flex: 1 — текст займає все вільне місце, спінер/стрілка притискаються вправо.
            Text(button.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            // ≈ loading ? <ActivityIndicator /> : (answer && !error ? <Ionicons chevron-up/down /> : null).
            // `when` без аргумента — як ланцюжок if / else if.
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                answer != null && !answer.error -> Icon(
                    if (isOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // ≈ {open[id] && answers[id] ? (error ? <Text style={error}> : <MarkdownText />) : null}.
        if (isOpen && answer != null) {
            val answerModifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            if (answer.error) {
                Text(answer.text, color = MaterialTheme.colorScheme.error, modifier = answerModifier)
            } else {
                MarkdownText(answer.text, modifier = answerModifier)
            }
        }
    }
}
