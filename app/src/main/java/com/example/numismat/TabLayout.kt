package com.example.numismat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.numismat.screens.HomeScreen
import com.example.numismat.screens.InfoScreen
import com.example.numismat.screens.ListScreen
import kotlinx.coroutines.launch

// Опис одного таба — як об'єкт { title, icon } для <Tabs.Screen>.
// `data class` ≈ TS-тип `{ title: string; icon: IconType }`.
data class Tab(val title: String, val icon: ImageVector)

// `listOf(...)` — незмінний масив (як `const tabs = [...] as const`).
// Icons.Filled.Home ≈ <Ionicons name="home" />.
val tabs = listOf(
    Tab("Головна", Icons.Filled.Home),
    Tab("Список", Icons.AutoMirrored.Filled.List),
    Tab("Інфо", Icons.Filled.Info),
)

// Аналог src/app/_layout.tsx з <Tabs>.
// В Expo Router <Tabs> сам малює хедер, таббар і перемикає екрани.
// У Compose це три окремі шматки, які ми збираємо в Scaffold:
//   topBar    — хедер з назвою (як header у Tabs),
//   bottomBar — NavigationBar (як tabBar),
//   контент   — HorizontalPager (місце, де показується активний екран).
//
// Чому Pager, а не NavHost: NavHost при перемиканні знищує екран (unmount),
// і стан у `remember` губиться. Pager тримає всі сторінки змонтованими —
// як React Navigation Tabs і SwiftUI TabView: стан і ефекти неактивних табів живуть.
// @OptIn — TopAppBar поки позначений як experimental API, тож явно погоджуємось.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabLayout() {
    // Стан пейджера — індекс активного таба (як useState<number>(0) для activeTab).
    // pageCount — лямбда, що повертає кількість сторінок.
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    // Скоуп для корутин — перемикання сторінки це suspend-функція (як async),
    // тож запускаємо її через scope.launch { ... } (як виклик async-функції без await).
    val scope = rememberCoroutineScope()

    // Кнопка "Назад" з будь-якого таба повертає на "Головна", а вже звідти закриває додаток —
    // як backBehavior: 'firstRoute' (типова поведінка Tabs у React Navigation на Android).
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.scrollToPage(0) }
    }

    Scaffold(
        // Хедер з назвою поточного таба — як options.title у Tabs.Screen.
        topBar = { TopAppBar(title = { Text(tabs[pagerState.currentPage].title) }) },
        bottomBar = {
            // NavigationBar — нижня панель табів (Material 3).
            NavigationBar {
                // forEachIndexed — як tabs.map((tab, index) => ...) у JSX.
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        // Активний таб підсвічується — Expo робить це сам, тут вказуємо вручну.
                        selected = pagerState.currentPage == index,
                        // Миттєве перемикання без анімації гортання — як звичайні таби.
                        onClick = { scope.launch { pagerState.scrollToPage(index) } },
                        // tabBarIcon: ({ color, size }) => <Ionicons ... /> —
                        // колір/розмір Compose підставляє сам з теми.
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            // Свайп між табами вимкнено — перемикаємо тільки через таббар.
            userScrollEnabled = false,
            // Тримати змонтованими ще 2 сторінки поза екраном = всі 3 таби живуть завжди.
            beyondViewportPageCount = tabs.size - 1,
            // padding(innerPadding) — щоб контент не залазив під хедер і таббар.
            modifier = Modifier.padding(innerPadding),
        ) { page ->
            // `when` — як switch у TS, але це вираз.
            when (page) {
                0 -> HomeScreen()
                1 -> ListScreen()
                2 -> InfoScreen()
            }
        }
    }
}
