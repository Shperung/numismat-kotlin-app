package com.example.numismat.lib

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.numismat.model.Coin
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

// Аналог `saveCoin(id | null, ...)` з src/lib/admin-actions.ts веб-версії:
// без id — нова монета, з id — оновлення існуючої.
// У вебі — REST з idToken адміна; тут SDK сам додає токен того, хто увійшов.
// Право на запис так само перевіряють Security Rules.
// `coin` — значення з форми; фото (`avers` / `revers`) — лише нововибрані, null — лишити як було.
// Повертає збережену монету з id і URL фото.
suspend fun saveCoin(context: Context, id: String?, coin: Coin, avers: Uri?, revers: Uri?): Coin {
    // У Firestore числа: ціле → integerValue, дробове → doubleValue (як `toFields` у вебі).
    // `Number` ≈ `number` у TS: тут або Long (25), або Double (0.5).
    val value = coin.value.toDouble()
    val number: Number = if (value % 1 == 0.0) value.toLong() else value
    val prefix = "coins/${coin.country}/$number-${coin.currency}-${coin.year}-${System.currentTimeMillis()}"

    // `coroutineScope { async {} ... }` ≈ `await Promise.all([...])`: обидва фото вантажаться паралельно.
    val (aversUrl, reversUrl) = coroutineScope {
        val a = async { avers?.let { uploadPhoto(context, "$prefix-avers", it) } }
        val r = async { revers?.let { uploadPhoto(context, "$prefix-revers", it) } }
        a.await() to r.await()
    }

    // `buildMap { put(...) }` ≈ збирання об'єкта `{ ...values, ...(aversUrl && { avers: aversUrl }) }`.
    val values = buildMap<String, Any> {
        put("country", coin.country)
        put("name", coin.name)
        put("value", number)
        put("currency", coin.currency)
        put("year", coin.year.toLong())
        aversUrl?.let { put("avers", it) }
        reversUrl?.let { put("revers", it) }
        when {
            coin.info != null -> put("info", coin.info)
            // Опис очистили при редагуванні → видалити поле (у вебі — "info" в updateMask без значення).
            id != null -> put("info", FieldValue.delete())
        }
    }

    val savedId = if (id == null) {
        // ≈ `await addDoc(collection(db, "coins"), values)`.
        db.collection("coins").add(values).await().id
    } else {
        // ≈ `updateDoc(...)`: змінює лише передані поля; якщо документа немає — помилка
        // (як `currentDocument.exists=true` у вебі).
        db.collection("coins").document(id).update(values).await()
        id
    }
    return coin.copy(
        id = savedId,
        value = "$number",
        avers = aversUrl ?: coin.avers,
        revers = reversUrl ?: coin.revers,
    )
}

// ≈ `uploadPhoto` у вебі. Uri з Photo Picker — не файл, а "посилання" content://, тип беремо в системи.
private suspend fun uploadPhoto(context: Context, path: String, uri: Uri): String {
    val type = context.contentResolver.getType(uri) ?: "image/jpeg"
    // "image/jpeg" → "jpg" (у вебі розширення бралося з імені файлу).
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "jpg"
    val ref = storage.reference.child("$path.$extension")
    ref.putFile(uri, StorageMetadata.Builder().setContentType(type).build()).await()
    // URL виду `.../o/coins%2F...?alt=media&token=...` — такий самий, як зберігає веб.
    return ref.downloadUrl.await().toString()
}
