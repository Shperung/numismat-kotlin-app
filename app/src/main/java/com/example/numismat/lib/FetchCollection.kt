package com.example.numismat.lib

import kotlinx.coroutines.tasks.await

// Аналог src/lib/fetch-collection.ts:
//   export async function fetchCollection(name: string) {
//     const snapshot = await getDocs(collection(db, name));
//     return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }));
//   }
// `suspend fun` ≈ `async function`: її можна "чекати" (await) лише з корутини.
// Map<String, Any?> ≈ Record<string, unknown> — поки без типу Coin, як і `doc.data()` у JS.
suspend fun fetchCollection(name: String): List<Map<String, Any?>> {
    // `.get()` повертає Firebase Task (≈ Promise), `.await()` — як `await` у JS.
    val snapshot = db.collection(name).get().await()
    // `mapOf("id" to doc.id) + doc.data` ≈ `{ id: doc.id, ...doc.data() }`.
    // `doc.data` може бути null (Kotlin це явно показує типом), тому `.orEmpty()`.
    return snapshot.documents.map { doc -> mapOf("id" to doc.id) + doc.data.orEmpty() }
}
