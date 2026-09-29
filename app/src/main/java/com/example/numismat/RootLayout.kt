package com.example.numismat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.numismat.screens.CoinScreen

// Аналог src/app/_layout.tsx:
//   <CountriesProvider>
//     <CoinsProvider>
//       <Stack>
//         <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
//         <Stack.Screen name="coin/[id]" options={{ title: 'Монета' }} />
//       </Stack>
//     </CoinsProvider>
//   </CountriesProvider>
@Composable
fun RootLayout() {
    // navController ≈ `router` з expo-router: через нього робимо navigate / back.
    val navController = rememberNavController()

    // Беремо ViewModel-и тут, ВИЩЕ за NavHost — це роль <CountriesProvider> і <CoinsProvider>.
    // Важливо: `viewModel()` всередині `composable(...) { }` дав би окремий екземпляр для кожного
    // екрана стеку (кожен екран має свій "скоуп"), і екран монети завантажив би монети вдруге.
    // Тому стан живе тут і передається в екрани параметрами.
    val countriesState by viewModel<CountriesViewModel>().state.collectAsState()
    // Усі монети — лише щоб екран монети знайшов потрібну за id (як `useCoins()` у coin/[id].tsx).
    val coinsState by viewModel<CoinsViewModel>().state.collectAsState()

    // NavHost ≈ <Stack>: показує один екран за раз, нові кладе зверху, "Назад" знімає верхній.
    // Роути — звичайні рядки, як шляхи файлів в Expo Router.
    NavHost(navController = navController, startDestination = "tabs") {
        // ≈ <Stack.Screen name="(tabs)" />: вкладені таби як один екран стеку.
        composable("tabs") {
            TabLayout(
                countriesState = countriesState,
                // ≈ router.push(`/coin/${id}`).
                onCoinClick = { id -> navController.navigate("coin/$id") },
            )
        }
        // "{id}" ≈ [id] у назві файлу — динамічний сегмент.
        composable("coin/{id}") { entry ->
            // ≈ `const { id } = useLocalSearchParams()`.
            val id = entry.arguments?.getString("id")
            CoinScreen(
                // `.find { }` ≈ `.find((c) => c.id === id)`; повертає null, якщо не знайдено.
                coin = coinsState.coins.find { it.id == id },
                // ≈ router.back(). Системну кнопку/жест "Назад" NavHost обробляє сам.
                onBack = { navController.popBackStack() },
            )
        }
    }
}
