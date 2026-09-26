package com.example.numismat.lib

import android.content.Context
import com.example.numismat.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore

// Аналог src/lib/firebase.ts:
//   const app = initializeApp({ apiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY, ... });
//   export const db = getFirestore(app);
// Різниця: в JS initializeApp викликається при імпорті модуля, а в Android йому потрібен
// Context (доступ до системи), тож викликаємо вручну з MainActivity.onCreate.
fun initFirebase(context: Context) {
    // onCreate викликається знову, напр. при повороті екрана, а повторна ініціалізація кидає помилку.
    if (FirebaseApp.getApps(context).isNotEmpty()) return

    val options = FirebaseOptions.Builder()
        .setApiKey(BuildConfig.FIREBASE_API_KEY)
        .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
        .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
        .setApplicationId(BuildConfig.FIREBASE_APP_ID)
        .build()
    FirebaseApp.initializeApp(context, options)
}

// `export const db = getFirestore(app)`.
// `by lazy` — значення створюється при першому зверненні (вже після initFirebase) і кешується.
val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
