package com.example.numismat.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.numismat.model.Country

// Аналог <Picker> з @expo/ui — на Android він і є цим Material 3 dropdown.
// Спільний для фільтра "Списку" і форми монети (у вебі — `<select>` у `CoinForm`).
// У Compose готового Picker немає, тож збираємо з частин:
//   ExposedDropdownMenuBox — контейнер, що зв'язує поле і меню,
//   OutlinedTextField (readOnly) — поле з вибраною країною і стрілкою,
//   ExposedDropdownMenu + DropdownMenuItem — випадаючий список (≈ Picker.Item).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPicker(countries: List<Country>, selectedId: String?, onSelect: (String) -> Unit) {
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
