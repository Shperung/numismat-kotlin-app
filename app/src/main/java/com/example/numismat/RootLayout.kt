package com.example.numismat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.numismat.model.Coin
import com.example.numismat.screens.CoinScreen
import com.example.numismat.screens.EditCoinScreen

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
    val coinsViewModel = viewModel<CoinsViewModel>()
    val coinsState by coinsViewModel.state.collectAsState()
    // Поточний адмін (null — не увійшли): таби показують "Увійти" або "Адмінка".
    val user by viewModel<AuthViewModel>().user.collectAsState()

    // ≈ `updateTag("coins"); redirect(`/coin/${id}`)` з `saveCoin` у вебі.
    // `popUpTo("tabs")` — зняти зі стеку все над табами (форму редагування, старий екран монети),
    // щоб "Назад" з нової сторінки монети вів на таби, а не назад у форму.
    val onCoinSaved = { coin: Coin ->
        coinsViewModel.save(coin)
        navController.navigate("coin/${coin.id}") { popUpTo("tabs") }
    }
    // Кнопка «Редагувати» — лише для адміна; null → кнопки немає.
    // Лямбда — останній вираз блоку `{ }` гілки if (≈ `user ? (id) => ... : null`).
    val onEditCoin: ((String) -> Unit)? = if (user != null) {
        { id -> navController.navigate("edit/$id") }
    } else null

    // NavHost ≈ <Stack>: показує один екран за раз, нові кладе зверху, "Назад" знімає верхній.
    // Роути — звичайні рядки, як шляхи файлів в Expo Router.
    NavHost(navController = navController, startDestination = "tabs") {
        // ≈ <Stack.Screen name="(tabs)" />: вкладені таби як один екран стеку.
        composable("tabs") {
            TabLayout(
                countriesState = countriesState,
                user = user,
                // ≈ router.push(`/coin/${id}`).
                onCoinClick = { id -> navController.navigate("coin/$id") },
                onCoinAdded = onCoinSaved,
                onEditCoin = onEditCoin,
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
                onEdit = onEditCoin,
            )
        }
        // ≈ /admin/edit/[id] у вебі.
        composable("edit/{id}") { entry ->
            val id = entry.arguments?.getString("id")
            EditCoinScreen(
                coin = coinsState.coins.find { it.id == id },
                countries = countriesState.countries,
                onBack = { navController.popBackStack() },
                onSaved = onCoinSaved,
            )
        }
    }
}
