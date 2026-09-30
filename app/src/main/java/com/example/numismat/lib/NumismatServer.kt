package com.example.numismat.lib

import com.example.numismat.model.Coin
import com.example.numismat.model.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Аналог src/lib/numismat-server.ts — запити до нашого AI-проксі numismat-server.
// Готового `fetch` в Android немає. Зазвичай беруть бібліотеку (OkHttp / Ktor Client),
// але для двох простих запитів вистачає вбудованих HttpURLConnection (HTTP) і org.json (JSON) — без нових залежностей.
private const val API_URL = "https://inua.tetiana-redko.com"

// ≈ `type Message = { role: 'user' | 'assistant'; content: string }`.
data class Message(val role: String, val content: String)

// ≈ `type Provider = { id: string; title: string; logo: string }`. logo — URL картинки.
data class Provider(val id: String, val title: String, val logo: String)

// ≈ fetchProviders(): GET /providers → список моделей, для яких показуємо кнопки.
suspend fun fetchProviders(): List<Provider> = withContext(Dispatchers.IO) {
    // GET — метод за замовчуванням, тож лише відкриваємо з'єднання і читаємо відповідь.
    val connection = URL("$API_URL/providers").openConnection() as HttpURLConnection
    try {
        if (connection.responseCode !in 200..299) throw Exception("${connection.responseCode}")
        val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        // У JSONArray немає `.map`, тому `List(size) { i -> ... }` — створити список, заповнивши його за індексом
        // (≈ `Array.from({ length }, (_, i) => ...)`).
        List(json.length()) { i ->
            val p = json.getJSONObject(i)
            Provider(p.getString("id"), p.getString("title"), p.getString("logo"))
        }
    } finally {
        connection.disconnect()
    }
}

// ≈ askServer(): POST /chat → текст відповіді вибраної моделі.
suspend fun askServer(provider: String, coin: Coin, messages: List<Message>): String =
    // В JS `fetch` сам не блокує UI. Тут мережевий виклик блокуючий, і на головному (UI) потоці
    // Android його забороняє (NetworkOnMainThreadException). `withContext(Dispatchers.IO)` виконує
    // блок у фоновому потоці для I/O, а корутина тим часом просто чекає — як `await`.
    withContext(Dispatchers.IO) {
        // ≈ JSON.stringify({ provider, coin, messages }).
        val body = JSONObject(
            mapOf(
                "provider" to provider,
                "coin" to coin.toJson(),
                "messages" to JSONArray(messages.map { JSONObject(mapOf("role" to it.role, "content" to it.content)) }),
            )
        )

        // ≈ fetch(`${API_URL}/chat`, { method: 'POST', headers: {...}, body }).
        val connection = URL("$API_URL/chat").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            // `.use { }` сам закриє потік після запису (≈ try/finally з close()).
            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            // ≈ res.ok. При помилці тіло відповіді читається з errorStream, а не inputStream.
            val ok = connection.responseCode in 200..299
            val stream = if (ok) connection.inputStream else connection.errorStream
            // ≈ await res.json().
            val data = JSONObject(stream.bufferedReader().use { it.readText() })
            if (!ok) throw Exception("${connection.responseCode} ${data.optString("error")}")
            // Останній вираз блоку — це і є результат (як `return data.text`).
            data.getString("text")
        } finally {
            connection.disconnect()
        }
    }
