package com.example.numismat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.numismat.lib.addCountry
import com.example.numismat.model.Country
import kotlinx.coroutines.launch

// Аналог src/components/country-form.tsx з веб-версії (поки лише нова країна, без редагування).
@Composable
fun CountryForm(onSaved: (Country) -> Unit) {
    var id by remember { mutableStateOf("") }
    var flag by remember { mutableStateOf("") }
    var nameUa by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    // ≈ `state.success` у вебі: "Додано 🇺🇦 Україна".
    var success by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun save() {
        val country = Country(
            id = id.trim().lowercase(),
            nameUa = nameUa.trim(),
            nameEn = nameEn.trim(),
            flag = flag.trim(),
        )
        // `Regex(...).matches` ≈ `/^[a-z]{2,3}$/.test(...)` — перевіряє весь рядок.
        if (!Regex("[a-z]{2,3}").matches(country.id) ||
            country.nameUa.isEmpty() || country.nameEn.isEmpty() || country.flag.isEmpty()
        ) {
            error = "Заповніть код (2–3 латинські літери), назви та прапор"
            success = null
            return
        }
        scope.launch {
            pending = true
            error = null
            success = null
            try {
                addCountry(country)
                id = ""
                flag = ""
                nameUa = ""
                nameEn = ""
                success = "Додано ${country.flag} ${country.nameUa}"
                onSaved(country)
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            }
            pending = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Нова країна", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = id,
                // `take(3)` ≈ `maxLength={3}`.
                onValueChange = { id = it.take(3) },
                label = { Text("Код") },
                placeholder = { Text("ua") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = flag,
                onValueChange = { flag = it },
                label = { Text("Прапор") },
                placeholder = { Text("🇺🇦") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = nameUa,
            onValueChange = { nameUa = it },
            label = { Text("Назва українською") },
            placeholder = { Text("Україна") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = nameEn,
            onValueChange = { nameEn = it },
            label = { Text("Назва англійською") },
            placeholder = { Text("Ukraine") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        success?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(onClick = { save() }, enabled = !pending, modifier = Modifier.fillMaxWidth()) {
            Text(if (pending) "Збереження..." else "Додати країну")
        }
    }
}
