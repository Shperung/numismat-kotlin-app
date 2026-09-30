package com.example.numismat.model

import org.json.JSONObject

// Аналог src/types/coin.ts.
// `String?` ≈ `info?: string` — поле може бути відсутнім (null).
// `= null` — значення за замовчуванням, тож при створенні його можна не передавати.
data class Coin(
    val id: String,
    val country: String,
    val name: String,
    // У Firestore value буває і числом, і рядком ("10"), тому тримаємо як рядок — нам його лише показувати.
    val value: String,
    val currency: String,
    // Як і value — лише для показу, тож рядок.
    val year: String,
    val info: String? = null,
    val avers: String? = null,
    val revers: String? = null,
)

// В TS ми просто пишемо `coins as Coin[]` — це лише "обіцянка" компілятору, в рантаймі нічого не перевіряється.
// Kotlin так не дозволяє, тому перекладаємо Map з Firestore в Coin руками.
// `as? String` — безпечне приведення: якщо там не рядок, буде null (а не краш).
// `?: ""` — якщо null, підставити порожній рядок (≈ `?? ''`).
fun coinFromMap(map: Map<String, Any?>) = Coin(
    id = map["id"] as String,
    country = map["country"] as? String ?: "",
    name = map["name"] as? String ?: "",
    value = map["value"]?.toString() ?: "",
    currency = map["currency"] as? String ?: "",
    year = map["year"]?.toString() ?: "",
    info = map["info"] as? String,
    avers = map["avers"] as? String,
    revers = map["revers"] as? String,
)

// Extension-функція: "дописуємо" метод до класу Coin, не змінюючи сам клас (у TS так не можна).
// ≈ JSON-об'єкт монети з полями як у TS-типі Coin — для numismat-server і для промпту Gemini.
fun Coin.toJson() = JSONObject(
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
