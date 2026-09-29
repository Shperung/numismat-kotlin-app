package com.example.numismat.lib

import com.example.numismat.model.Coin
import com.example.numismat.model.coinFromMap
import kotlinx.coroutines.tasks.await

// Аналог src/lib/fetch-coins-by-country.ts:
//   const snapshot = await getDocs(query(collection(db, 'coins'), where('country', '==', country)));
// В Android SDK query будується ланцюжком: `.whereEqualTo(...)` ≈ `where(..., '==', ...)`.
suspend fun fetchCoinsByCountry(country: String): List<Coin> {
    val snapshot = db.collection("coins").whereEqualTo("country", country).get().await()
    return snapshot.documents.map { doc -> coinFromMap(mapOf("id" to doc.id) + doc.data.orEmpty()) }
}
