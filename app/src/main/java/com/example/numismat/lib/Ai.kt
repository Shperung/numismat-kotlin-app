package com.example.numismat.lib

import android.content.Context
import android.net.Uri
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import com.example.numismat.model.toJson
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Аналог `const ai = getAI(app, { backend: new AgentPlatformBackend('global') })`.
// `by lazy` — як і `db` у Firebase.kt: створиться при першому зверненні, вже після initFirebase.
private val ai by lazy { Firebase.ai(backend = GenerativeBackend.agentPlatform("global")) }

// Аналог src/lib/ai.ts: модель з інструкцією "про цю монету" + новий чат.
fun startCoinChat(coin: Coin) = ai.generativeModel(
    modelName = "gemini-3.5-flash-lite",
    // `content { text(...) }` — повідомлення для моделі (в JS можна просто рядок).
    systemInstruction = content {
        // Той самий промпт, що на numismat-server (systemPrompt). `${coin.toJson()}` ≈ `${JSON.stringify(coin)}`.
        text(
            "Ти досвідчений нумізмат. Відповідай українською, коротко і цікаво. " +
                "Розмова про монету: ${coin.toJson()}, які факти про неї є, чи вона ще в вжитку, " +
                "що за неї можна купити або можна було купити у рік виходу"
        )
    },
).startChat()

// ≈ `draftSchema` в Expo: Gemini поверне JSON саме такої форми (без newCountry — країну додаємо формою вручну).
private val draftSchema = Schema.obj(
    mapOf(
        "name" to Schema.string("Назва монети англійською, напр. «Quarter Dollar», «One Cent»"),
        "country" to Schema.string("Код країни: зі списку наявних або новий ISO 3166-1 alpha-2, нижній регістр"),
        "value" to Schema.double(
            "Номінал числом в одиницях currency; для розмінних монет — у дрібних: 25 (cent), а не 0.25 (dollar)"
        ),
        "currency" to Schema.string(
            "Одиниця номіналу англійською в однині: дрібна для розмінних (cent, kopiyka), основна для інших (dollar, hryvnia)"
        ),
        "year" to Schema.integer("Рік карбування з фото"),
        "info" to Schema.string("Опис українською: що зображено на аверсі й реверсі, написи, 2–3 речення"),
    )
)

// Аналог `describeCoinPhotos` з Expo: два фото → чернетка монети для форми.
// `avers` / `revers` — content:// з Photo Picker або URL зі Storage (при редагуванні).
suspend fun describeCoinPhotos(context: Context, avers: String, revers: String, countries: List<Country>): Coin {
    val model = ai.generativeModel(
        modelName = "gemini-3.5-flash",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = draftSchema
        },
    )
    val known = countries.joinToString { "${it.id} — ${it.nameEn}" }
    // Читаємо фото заздалегідь: лямбда `content { }` — звичайна, не suspend, у ній `readPhoto` викликати не можна.
    val aversBytes = readPhoto(context, avers)
    val reversBytes = readPhoto(context, revers)
    val response = model.generateContent(
        content {
            text(
                "Ти досвідчений нумізмат. Перше фото — аверс монети, друге — реверс. Визнач монету й заповни дані. " +
                    "Наявні країни: $known. Якщо країна монети серед них — візьми її код."
            )
            // ≈ `{ inlineData: { data, mimeType } }`; base64 SDK робить сам — передаємо байти.
            inlineData(aversBytes, photoType(context, avers))
            inlineData(reversBytes, photoType(context, revers))
        }
    )
    val json = JSONObject(response.text ?: throw Exception("Gemini не повернув відповідь"))
    // 25.0 → "25", 0.5 → "0.5" — як номінал у формі.
    val value = json.getDouble("value")
    return Coin(
        id = "",
        country = json.getString("country"),
        name = json.getString("name"),
        value = if (value % 1 == 0.0) "${value.toLong()}" else "$value",
        currency = json.getString("currency"),
        year = "${json.getInt("year")}",
        info = json.optString("info").ifBlank { null },
    )
}

// ≈ `toPart` в Expo (`fetch(uri).blob()`): байти фото — з галереї через contentResolver або з мережі.
// Мережа і читання файлів — не на UI-потоці, тому `Dispatchers.IO`.
private suspend fun readPhoto(context: Context, uri: String): ByteArray = withContext(Dispatchers.IO) {
    if (uri.startsWith("http")) {
        URL(uri).readBytes()
    } else {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
            ?: throw Exception("Не вдалося прочитати фото")
    }
}

// Галерея може дати PNG / HEIC; збережені в Storage фото — jpg.
private fun photoType(context: Context, uri: String) =
    if (uri.startsWith("http")) "image/jpeg" else context.contentResolver.getType(Uri.parse(uri)) ?: "image/jpeg"
