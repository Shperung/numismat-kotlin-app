package com.example.numismat.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Аналог src/app/index.tsx в Expo:
//   export default function HomeScreen() {
//     return <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
//       <Text>Головна</Text>
//     </View>
//   }
// Різниця: в Expo екран = файл (file-based routing), а тут екран — звичайна функція,
// яку ми вручну реєструємо в NavHost (див. TabLayout.kt).
@Composable
fun HomeScreen() {
    // Box — як <View>. fillMaxSize() = flex: 1,
    // contentAlignment = Alignment.Center = alignItems + justifyContent: 'center'.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Головна")
    }
}
