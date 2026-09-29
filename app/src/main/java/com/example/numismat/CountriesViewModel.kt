package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCollection
import com.example.numismat.model.Country
import com.example.numismat.model.countryFromMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Аналог типу CountriesState з countries-provider.tsx.
data class CountriesState(
    val countries: List<Country> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

// Аналог src/providers/countries-provider.tsx (CountriesProvider + useCountries).
// Країни потрібні і Головній, і "Списку", тому, як і CoinsViewModel, створюється в RootLayout
// і передається в екрани параметрами.
class CountriesViewModel : ViewModel() {
    private val _state = MutableStateFlow(CountriesState())
    val state: StateFlow<CountriesState> = _state

    init {
        viewModelScope.launch {
            _state.value = try {
                CountriesState(countries = fetchCollection("countries").map { countryFromMap(it) }, loading = false)
            } catch (e: Exception) {
                CountriesState(loading = false, error = e.toString())
            }
        }
    }
}
