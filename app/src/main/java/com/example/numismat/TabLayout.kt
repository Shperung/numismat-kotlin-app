package com.example.numismat

import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.numismat.screens.HomeScreen
import com.example.numismat.screens.InfoScreen
import com.example.numismat.screens.ListScreen

// Опис одного таба — як об'єкт { name, title, icon } для <Tabs.Screen>.
// `data class` ≈ TS-тип `{ route: string; title: string; icon: IconType }`.
// route — рядковий "шлях" екрана, як ім'я файлу в Expo Router ("index", "list", "info").
data class Tab(val route: String, val title: String, val icon: ImageVector)

// `listOf(...)` — незмінний масив (як `const tabs = [...] as const`).
// Icons.Filled.Home ≈ <Ionicons name="home" />.
val tabs = listOf(
    Tab("index", "Головна", Icons.Filled.Home),
    Tab("list", "Список", Icons.AutoMirrored.Filled.List),
    Tab("info", "Інфо", Icons.Filled.Info),
)

// Аналог src/app/_layout.tsx з <Tabs>.
// В Expo Router <Tabs> сам малює хедер, таббар і перемикає екрани.
// У Compose це три окремі шматки, які ми збираємо в Scaffold:
//   topBar    — хедер з назвою (як header у Tabs),
//   bottomBar — NavigationBar (як tabBar),
//   контент   — NavHost (як <Slot /> / місце, де рендериться активний екран).
// @OptIn — TopAppBar поки позначений як experimental API, тож явно погоджуємось.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabLayout() {
    // NavController — "двигун" навігації, як об'єкт navigation / router з useRouter().
    // remember... — створюється один раз і живе між рекомпозиціями (як useRef/useState).
    val navController = rememberNavController()

    // Поточний маршрут — аналог usePathname() / useSegments().
    // `by` — делегат: читаємо значення стану напряму, без `.value`.
    // Коли маршрут змінюється, TabLayout перемальовується (як ре-рендер при зміні стану).
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // `?.` і `?:` — як `?.` і `??` у TS.
    val currentTab = tabs.find { it.route == currentRoute } ?: tabs.first()

    Scaffold(
        // Хедер з назвою поточного таба — як options.title у Tabs.Screen.
        topBar = { TopAppBar(title = { Text(currentTab.title) }) },
        bottomBar = {
            // NavigationBar — нижня панель табів (Material 3).
            NavigationBar {
                // forEach — як tabs.map(tab => <Tabs.Screen ... />) у JSX.
                tabs.forEach { tab ->
                    NavigationBarItem(
                        // Активний таб підсвічується — Expo робить це сам, тут вказуємо вручну.
                        selected = tab.route == currentRoute,
                        onClick = {
                            // Як router.navigate('/list'), але з налаштуваннями "як у табів":
                            navController.navigate(tab.route) {
                                // Не накопичувати екрани в стеку при перемиканні табів
                                // (інакше "Назад" ходив би по всіх натиснутих табах).
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Повторний тап по активному табу не створює дубль екрана.
                                launchSingleTop = true
                                // Повертаючись на таб, відновити його стан (скрол тощо).
                                restoreState = true
                            }
                        },
                        // tabBarIcon: ({ color, size }) => <Ionicons ... /> —
                        // колір/розмір Compose підставляє сам з теми.
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost — "карта маршрутів". В Expo її будує файлова система (src/app/*.tsx),
        // тут реєструємо вручну: route -> composable-екран.
        // startDestination = "index" — як index.tsx, що відкривається першим.
        // padding(innerPadding) — щоб контент не залазив під хедер і таббар.
        NavHost(
            navController = navController,
            startDestination = "index",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("index") { HomeScreen() }
            composable("list") { ListScreen() }
            composable("info") { InfoScreen() }
        }
    }
}
