package com.example.numismat.lib

import com.example.numismat.model.Coin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Аналог src/lib/numismat-server.ts — запит до нашого AI-проксі numismat-server.
private const val API_URL = "https://inua.tetiana-redko.com"

// ≈ `type Message = { role: 'user' | 'assistant'; content: string }`.
data class Message(val role: String, val content: String)

// Готового `fetch` в Android немає. Зазвичай беруть бібліотеку (OkHttp / Ktor Client),
// але для одного POST вистачає вбудованих HttpURLConnection (HTTP) і org.json (JSON) — без нових залежностей.
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

// Extension-функція: "дописуємо" метод до класу Coin, не змінюючи сам клас (у TS так не можна).
// Поля — як у TS-типі Coin, щоб сервер отримав той самий JSON, що й від Expo.
private fun Coin.toJson() = JSONObject(
    mapOf(
        "id" to id,
        "country" to country,
        "name" to name,
        "value" to value,
        "currency" to currency,
        "year" to year,
        "info" to info,
        "avers" to avers,
        "revers" to revers,
    )
)
