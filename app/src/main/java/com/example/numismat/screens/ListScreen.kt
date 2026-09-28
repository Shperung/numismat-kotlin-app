package com.example.numismat.screens
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.numismat.CountriesViewModel
import org.json.JSONArray

// Аналог src/app/list.tsx в Expo.
@Composable
fun ListScreen() {

    val state by viewModel<CountriesViewModel>().state.collectAsState()

    SelectionContainer {
        Text(
            text = if (state.loading) "Завантаження..."
            else state.error ?: JSONArray(state.countries).toString(2),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            // verticalScroll ≈ <ScrollView>: у Compose скрол — це модифікатор, а не окремий компонент.
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        )
    }
}
