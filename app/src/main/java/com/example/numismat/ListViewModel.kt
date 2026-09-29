package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCoinsByCountry
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Стан екрана "Список" — useState з list.tsx, зібрані в один об'єкт (countries тепер приходять ззовні).
// Окремого `loading` немає, як і в Expo: поки країну не вибрано (`country == null`), показуємо "Завантаження...".
data class ListState(
    // id вибраної країни (≈ `useState<string>()`).
    val country: String? = null,
    val coins: List<Coin> = emptyList(),
    val error: String? = null,
)

// В Expo ця логіка живе прямо в ListScreen (useState + useEffect).
// Тут — у ViewModel: при переході на екран монети таби зникають з екрана, і `remember`-стан загубився б
// (при поверненні — нова випадкова країна і повторні запити). ViewModel живе, поки таби є в стеку навігації.
class ListViewModel : ViewModel() {
    private val _state = MutableStateFlow(ListState())
    val state: StateFlow<ListState> = _state

    // Поточний запит монет. `Job` — "ручка" запущеної корутини, через неї її можна скасувати.
    private var coinsJob: Job? = null

    // ≈ useEffect(() => setCountry(pickRandom(countries)?.id), [countries]).
    // Екран викликає це щоразу, коли з'являється на екрані, тому вибираємо лише один раз.
    fun selectRandomCountry(countries: List<Country>) {
        if (_state.value.country != null) return
        // `randomOrNull()` ≈ pickRandom: вбудований у Kotlin, null для порожнього списку.
        // `?.let { ... }` — виконати блок, тільки якщо значення не null (≈ `if (x) { ... }`).
        countries.randomOrNull()?.let { selectCountry(it.id) }
    }

    // ≈ `setCountry` + useEffect(..., [country]).
    fun selectCountry(id: String) {
        // `update { it.copy(...) }` ≈ `setState((s) => ({ ...s, country: id }))`.
        // `copy` — вбудований метод data class: копія об'єкта зі зміненими полями.
        _state.update { it.copy(country = id) }
        // В Expo прапорець `active = false` у cleanup ігнорує відповідь старого запиту, якщо країну вже змінили.
        // Тут просто скасовуємо попередню корутину — її результат уже ніколи не потрапить у стан.
        coinsJob?.cancel()
        coinsJob = viewModelScope.launch {
            try {
                val coins = fetchCoinsByCountry(id)
                _state.update { it.copy(coins = coins) }
            } catch (e: Exception) {
                // Скасування корутини — теж виняток (CancellationException). Це не помилка, тож пропускаємо його далі.
                if (e is CancellationException) throw e
                _state.update { it.copy(error = e.toString()) }
            }
        }
    }
}
