package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCoinsByCountry
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// В Expo `useState<Coin | null>()` має три стани: undefined — вантажиться, null — не знайдено, Coin — є.
// У Kotlin немає різниці між undefined і null, тому "вантажиться" — окремим полем `loading`.
data class HomeState(
    val coin: Coin? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

// Логіка з src/app/(tabs)/index.tsx. У ViewModel з тієї ж причини, що й ListViewModel:
// щоб при поверненні з екрана монети на Головній не з'являлась щоразу нова монета.
class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private var started = false

    // ≈ useEffect(() => { ... }, [countries]): випадкова країна → її монети → випадкова монета.
    fun showRandomCoin(countries: List<Country>) {
        if (started) return
        // `?: return` — "якщо null, вийти з функції" (≈ `if (!country) return;`).
        val country = countries.randomOrNull() ?: return
        started = true
        viewModelScope.launch {
            _state.value = try {
                HomeState(coin = fetchCoinsByCountry(country.id).randomOrNull(), loading = false)
            } catch (e: Exception) {
                HomeState(loading = false, error = e.toString())
            }
        }
    }
}
