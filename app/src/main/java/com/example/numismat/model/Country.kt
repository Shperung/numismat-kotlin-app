package com.example.numismat.model

// Аналог src/types/country.ts. Документ `countries/{id}` = { name_ua, name_en, flag }.
// У Kotlin імена полів прийнято писати camelCase, тому `name_ua` → `nameUa` (перекладаємо в countryFromMap).
data class Country(
    val id: String,
    val nameUa: String,
    val nameEn: String,
    // Прапор — емодзі, наприклад "🇺🇦".
    val flag: String,
)

fun countryFromMap(map: Map<String, Any?>) = Country(
    id = map["id"] as String,
    nameUa = map["name_ua"] as? String ?: "",
    nameEn = map["name_en"] as? String ?: "",
    flag = map["flag"] as? String ?: "",
)
