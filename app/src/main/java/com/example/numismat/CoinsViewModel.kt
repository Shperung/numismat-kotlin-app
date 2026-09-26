package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Аналог типу CoinsState з coins-provider.tsx.
data class CoinsState(
    val coins: List<Map<String, Any?>> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

// Аналог src/providers/coins-provider.tsx (CoinsProvider + useCoins).
// В Expo стан живе в Context, який обгортає <Tabs> у _layout.tsx.
// В Android замість Context — ViewModel: один екземпляр на Activity, переживає поворот екрана.
// Будь-який екран отримує той самий екземпляр через `viewModel()` — як `useCoins()`.
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
                CoinsState(coins = fetchCollection("coins"), loading = false)
            } catch (e: Exception) {
                CoinsState(loading = false, error = e.toString())
            }
        }
    }
}
