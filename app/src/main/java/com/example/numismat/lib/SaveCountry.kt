package com.example.numismat.lib

import com.example.numismat.model.Country
import kotlinx.coroutines.tasks.await

// Аналог `addCountry` з src/lib/admin.ts в Expo: код країни = id документа `countries/{id}`.
// `set()` перезаписав би існуючу країну, тому спершу перевіряємо (у вебі — помилка "already exists").
suspend fun addCountry(country: Country) {
    val ref = db.collection("countries").document(country.id)
    if (ref.get().await().exists()) throw Exception("Країна «${country.id}» вже існує")
    ref.set(mapOf("name_ua" to country.nameUa, "name_en" to country.nameEn, "flag" to country.flag)).await()
}
