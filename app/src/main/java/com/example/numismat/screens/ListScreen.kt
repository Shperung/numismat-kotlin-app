package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.numismat.CountriesState
import com.example.numismat.ListViewModel
import com.example.numismat.components.CoinCard
import com.example.numismat.components.CountryPicker

// Аналог src/app/(tabs)/list.tsx: фільтр монет за країною.
// `countriesState` ≈ `useCountries()`, `onCoinClick` — тап по картці відкриває екран монети.
@Composable
fun ListScreen(countriesState: CountriesState, onCoinClick: (String) -> Unit) {
    // Тут `viewModel()` можна викликати прямо в екрані: цей стан потрібен лише "Списку".
    val viewModel = viewModel<ListViewModel>()
    val state by viewModel.state.collectAsState()

    // ≈ useEffect(() => setCountry(pickRandom(countries)?.id), [countries]).
    LaunchedEffect(countriesState.countries) {
        viewModel.selectRandomCountry(countriesState.countries)
    }

    // ≈ `if (countriesError || error) return ...; if (!country) return <Text>Завантаження...</Text>`.
    if (countriesState.error != null || state.error != null || state.country == null) {
        Text(countriesState.error ?: state.error ?: "Завантаження...", modifier = Modifier.padding(16.dp))
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // `item { }` — один довільний елемент списку. Перший = ListHeaderComponent.
        item {
            CountryPicker(
                countries = countriesState.countries,
                selectedId = state.country,
                onSelect = { id -> viewModel.selectCountry(id) },
            )
        }
        // ≈ ListEmptyComponent. У LazyColumn такого пропа немає — просто умовний item.
        if (state.coins.isEmpty()) {
            item { Text("Монет цієї країни немає") }
        }
        items(state.coins, key = { it.id }) { coin ->
            CoinCard(coin, onClick = { onCoinClick(coin.id) })
        }
    }
}
