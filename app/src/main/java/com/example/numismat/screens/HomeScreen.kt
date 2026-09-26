package com.example.numismat.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.numismat.CoinsViewModel
import org.json.JSONArray

// Аналог src/app/index.tsx в Expo:
//   const { coins, loading, error } = useCoins();
//   <ScrollView contentContainerStyle={{ padding: 16 }}>
//     <Text selectable style={{ fontFamily: 'Menlo', fontSize: 12 }}>
//       {loading ? 'Завантаження...' : error ?? JSON.stringify(coins, null, 2)}
//     </Text>
//   </ScrollView>
@Composable
fun HomeScreen() {
    // `viewModel()` ≈ `useCoins()`: повертає спільний на всю Activity CoinsViewModel.
    // `collectAsState()` підписує екран на StateFlow — при зміні стану буде ре-рендер.
    // `by` розпаковує State, щоб писати `state.coins`, а не `state.value.coins`.
    val state by viewModel<CoinsViewModel>().state.collectAsState()

    // `SelectionContainer` ≈ `selectable` у <Text>.
    SelectionContainer {
        Text(
            // `if` у Kotlin — вираз, тому замінює тернарник `? :` з TS.
            // `?:` (elvis) ≈ `??` у TS. JSONArray(...).toString(2) ≈ JSON.stringify(coins, null, 2).
            text = if (state.loading) "Завантаження..."
            else state.error ?: JSONArray(state.coins).toString(2),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            // verticalScroll ≈ <ScrollView>: у Compose скрол — це модифікатор, а не окремий компонент.
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        )
    }
}
