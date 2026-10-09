package com.example.numismat

import androidx.lifecycle.ViewModel
import com.example.numismat.lib.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Поточний адмін (null — не увійшли). У вебі це cookie `admin_session` + `hasSession()`:
// там сервер Next спільний для всіх, тож Firebase Auth SDK не підходить. Тут додаток на одному пристрої —
// SDK сам зберігає сесію між запусками і сам оновлює токен (замість refreshToken у cookie).
// Створюється в RootLayout, бо користувач потрібен і табам, і (далі) екрану монети.
class AuthViewModel : ViewModel() {
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    // ≈ `onAuthStateChanged(auth, (u) => setUser(u))` — викликається після входу і виходу.
    private val listener = FirebaseAuth.AuthStateListener { _user.value = it.currentUser }

    init {
        auth.addAuthStateListener(listener)
    }

    // ≈ cleanup `return unsubscribe` в useEffect: ViewModel знищується — відписуємось.
    override fun onCleared() {
        auth.removeAuthStateListener(listener)
    }
}
