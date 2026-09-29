package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCoinsByCountry
import com.example.numismat.lib.fetchCollection
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import com.example.numismat.model.countryFromMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Стан екрана "Список" — це чотири useState з list.tsx, зібрані в один об'єкт.
// Окремого `loading` немає, як і в Expo: поки країну не вибрано (`country == null`), показуємо "Завантаження...".
data class CountriesState(
    val countries: List<Country> = emptyList(),
    // id вибраної країни (≈ `useState<string>()`).
    val country: String? = null,
    val coins: List<Coin> = emptyList(),
    val error: String? = null,
)

// В Expo ця логіка живе прямо в ListScreen (useState + useEffect).
// Тут — у ViewModel: при переході на екран монети таби зникають з екрана, і `remember`-стан загубився б
// (при поверненні — нова випадкова країна і повторні запити). ViewModel живе, поки таби є в стеку навігації.
class CountriesViewModel : ViewModel() {
    private val _state = MutableStateFlow(CountriesState())
    val state: StateFlow<CountriesState> = _state

    // Поточний запит монет. `Job` — "ручка" запущеної корутини, через неї її можна скасувати.
    private var coinsJob: Job? = null

    // ≈ перший useEffect(..., []): завантажити країни і вибрати випадкову.
    init {
        viewModelScope.launch {
            try {
                val countries = fetchCollection("countries").map { countryFromMap(it) }
                // `update { it.copy(...) }` ≈ `setState((s) => ({ ...s, countries }))`.
                // `copy` — вбудований метод data class: копія об'єкта зі зміненими полями.
                _state.update { it.copy(countries = countries) }
                // `randomOrNull()` ≈ `list[Math.floor(Math.random() * list.length)]`, null для порожнього списку.
                // `?.let { ... }` — виконати блок, тільки якщо значення не null (≈ `if (x) { ... }`).
                countries.randomOrNull()?.let { selectCountry(it.id) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.toString()) }
            }
        }
    }

    // ≈ `setCountry` + другий useEffect(..., [country]).
    fun selectCountry(id: String) {
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
