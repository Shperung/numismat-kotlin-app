package com.example.numismat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.numismat.CountriesState
import com.example.numismat.ListViewModel
import com.example.numismat.components.CoinCard
import com.example.numismat.model.Country

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

// Аналог <Picker> з @expo/ui — на Android він і є цим Material 3 dropdown.
// У Compose готового Picker немає, тож збираємо з частин:
//   ExposedDropdownMenuBox — контейнер, що зв'язує поле і меню,
//   OutlinedTextField (readOnly) — поле з вибраною країною і стрілкою,
//   ExposedDropdownMenu + DropdownMenuItem — випадаючий список (≈ Picker.Item).
// `private` — функція видна лише в цьому файлі (як не-export у TS-модулі).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPicker(countries: List<Country>, selectedId: String?, onSelect: (String) -> Unit) {
    // `remember { mutableStateOf(false) }` ≈ `const [expanded, setExpanded] = useState(false)`.
    // Завдяки `by` читаємо і пишемо як звичайну змінну: `expanded = true` ≈ `setExpanded(true)`.
    var expanded by remember { mutableStateOf(false) }
    val selected = countries.find { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            // ≈ label={`${c.flag} ${c.name_ua}`}.
            value = if (selected != null) "${selected.flag} ${selected.nameUa}" else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Країна") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            // menuAnchor — "прив'язати" меню до цього поля (відкривається під ним, по тапу).
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // ≈ countries.map((c) => <Picker.Item ... />).
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text("${country.flag} ${country.nameUa}") },
                    onClick = {
                        onSelect(country.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
