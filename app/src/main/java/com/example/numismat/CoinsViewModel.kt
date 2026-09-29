package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCollection
import com.example.numismat.model.Coin
import com.example.numismat.model.coinFromMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Аналог типу CoinsState з coins-provider.tsx.
data class CoinsState(
    val coins: List<Coin> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

// Аналог src/providers/coins-provider.tsx (CoinsProvider + useCoins).
// В Expo стан живе в Context, який обгортає <Tabs> у _layout.tsx.
// В Android замість Context — ViewModel: один екземпляр на Activity, переживає поворот екрана.
// Створюється в RootLayout (вище за NavHost), а екрани отримують стан параметрами.
class CoinsViewModel : ViewModel() {
    // `useState<CoinsState>(...)`: MutableStateFlow — змінюване значення всередині,
    // назовні віддаємо лише StateFlow (read-only), щоб екрани не міняли стан напряму.
    private val _state = MutableStateFlow(CoinsState())
    val state: StateFlow<CoinsState> = _state

    // `init` виконується при створенні ViewModel — як `useEffect(() => {...}, [])` у провайдері.
    init {
        // viewModelScope.launch — запуск корутини, яка скасується, коли ViewModel знищиться.
        viewModelScope.launch {
            // try/catch ≈ `.then(...).catch(...)`.
            _state.value = try {
                // `.map { coinFromMap(it) }` ≈ `.map((it) => coinFromMap(it))`; `it` — ім'я параметра за замовчуванням.
                CoinsState(coins = fetchCollection("coins").map { coinFromMap(it) }, loading = false)
            } catch (e: Exception) {
                CoinsState(loading = false, error = e.toString())
            }
        }
    }
}
