package com.example.numismat.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.numismat.CountriesState
import com.example.numismat.HomeViewModel
import com.example.numismat.components.CoinDetails

// Аналог src/app/(tabs)/index.tsx: випадкова монета випадкової країни.
// `countriesState` ≈ `useCountries()` — приходить параметром з RootLayout.
@Composable
fun HomeScreen(countriesState: CountriesState) {
    val viewModel = viewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()

    // LaunchedEffect(key) ≈ useEffect(() => {...}, [countries]): запускається при появі на екрані
    // і щоразу, коли змінився key. Спершу countries порожні, після завантаження — спрацює ще раз.
    LaunchedEffect(countriesState.countries) {
        viewModel.showRandomCoin(countriesState.countries)
    }

    // Копія в локальну змінну — щоб спрацював smart cast: після `coin != null` Kotlin знає, що це `Coin`.
    // З `state.coin` напряму так не вийде: `state` — делегат (`by`), і компілятор не гарантує, що значення не зміниться.
    val coin = state.coin
    if (coin != null) {
        CoinDetails(coin)
    } else {
        // ≈ `countriesError ?? error ?? (coin === null ? 'Монет не знайдено' : 'Завантаження...')`.
        val message = countriesState.error ?: state.error
            ?: if (state.loading) "Завантаження..." else "Монет не знайдено"
        Text(message, modifier = Modifier.padding(16.dp))
    }
}
