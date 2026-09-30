package com.example.numismat.lib

import com.example.numismat.model.Coin
import com.example.numismat.model.toJson
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

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
