package com.example.numismat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numismat.lib.fetchCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class CountriesState(
    val countries: List<Map<String, Any?>> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

class CountriesViewModel: ViewModel() {
    private val _state = MutableStateFlow(CountriesState())
    val state: StateFlow<CountriesState> = _state

    init {
        // viewModelScope.launch — запуск корутини, яка скасується, коли ViewModel знищиться.
        viewModelScope.launch {
            // try/catch ≈ `.then(...).catch(...)`.
            _state.value = try {
                CountriesState(countries = fetchCollection("countries"), loading = false)
            } catch (e: Exception) {
                CountriesState(loading = false, error = e.toString())
            }
        }
    }
}