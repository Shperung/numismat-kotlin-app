package com.example.numismat.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numismat.lib.saveCoin
import com.example.numismat.model.Coin
import com.example.numismat.model.Country
import kotlinx.coroutines.launch

// Аналог src/components/coin-form.tsx з веб-версії (без Gemini і «Покращити»).
// `editing` — монета для редагування (≈ проп `editing?: Coin`), null — нова монета.
// `onSaved` — монету збережено; куди переходити, вирішує RootLayout (у вебі — `redirect('/coin/{id}')`).
@Composable
fun CoinForm(
    countries: List<Country>,
    onSaved: (Coin) -> Unit,
    modifier: Modifier = Modifier,
    editing: Coin? = null,
) {
    // `Uri?` — нововибране фото (≈ File з `<input type="file">`), null — не вибрано
    // (при редагуванні тоді лишається збережене).
    var avers by remember { mutableStateOf<Uri?>(null) }
    var revers by remember { mutableStateOf<Uri?>(null) }
    // Початкові значення — з монети, що редагується (≈ `defaultValue={coin?.name}`).
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var country by remember { mutableStateOf(editing?.country) }
    var value by remember { mutableStateOf(editing?.value ?: "") }
    var currency by remember { mutableStateOf(editing?.currency ?: "") }
    var year by remember { mutableStateOf(editing?.year ?: "") }
    var info by remember { mutableStateOf(editing?.info ?: "") }
    // ≈ `const [state, action, pending] = useActionState(saveCoin, null)`.
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Context потрібен, щоб прочитати вибране фото (≈ доступ до системи, якого в браузері не видно).
    val context = LocalContext.current

    fun save() {
        // Локальні `val` — щоб спрацював smart cast (з делегатом `by` він не працює).
        val countryId = country
        val aversUri = avers
        val reversUri = revers
        // `toDoubleOrNull()` ≈ `Number(...)`, але замість NaN — null. Кома — бо клавіатура Samsung дає ",".
        val number = value.trim().replace(',', '.')
        val yearNumber = year.trim().toIntOrNull()
        // Ті самі перевірки й тексти, що в `saveCoin` у вебі.
        if (countryId == null || name.isBlank() || currency.isBlank() ||
            !((number.toDoubleOrNull() ?: 0.0) > 0) || yearNumber == null || yearNumber <= 0
        ) {
            error = "Заповніть назву, країну, номінал, валюту та рік"
            return
        }
        // При редагуванні фото необовʼязкові — як `if (!id && (!avers || !revers))` у вебі.
        if (editing == null && (aversUri == null || reversUri == null)) {
            error = "Додайте фото аверсу і реверсу"
            return
        }
        val coin = Coin(
            id = "",
            country = countryId,
            name = name.trim(),
            value = number,
            currency = currency.trim(),
            year = "$yearNumber",
            // `ifBlank { null }` — порожній опис не зберігаємо (як `...(info && { info })`).
            info = info.trim().ifBlank { null },
            // Збережені фото — якщо нових не вибрали.
            avers = editing?.avers,
            revers = editing?.revers,
        )

        scope.launch {
            pending = true
            error = null
            try {
                val saved = saveCoin(context, editing?.id, coin, aversUri, reversUri)
                // Нову монету — очистити форму лише після успіху (у вебі — `<form key={state?.success}>`).
                // Країну лишаємо — зручно додавати кілька монет однієї країни.
                if (editing == null) {
                    avers = null
                    revers = null
                    name = ""
                    value = ""
                    currency = ""
                    year = ""
                    info = ""
                }
                onSaved(saved)
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            }
            pending = false
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (editing != null) "Редагування монети" else "Нова монета", style = MaterialTheme.typography.titleLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            // Превʼю: нововибране фото або збережене (у вебі — `initial={editing?.avers}`).
            PhotoInput("Аверс", avers?.toString() ?: editing?.avers) { avers = it }
            PhotoInput("Реверс", revers?.toString() ?: editing?.revers) { revers = it }
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Назва") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        CountryPicker(countries, country) { country = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Номінал") },
                singleLine = true,
                // ≈ `type="number" step="any"`.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                // weight(1f) ≈ flex: 1 — три поля ділять рядок порівну.
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Валюта") },
                placeholder = { Text("cent, копійка...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Рік") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = info,
            onValueChange = { info = it },
            label = { Text("Опис") },
            // ≈ `<textarea rows={4}>`.
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { save() }, enabled = !pending, modifier = Modifier.fillMaxWidth()) {
            Text(
                when {
                    pending -> "Збереження..."
                    editing != null -> "Зберегти зміни"
                    else -> "Додати монету"
                }
            )
        }
    }
}

// Аналог `PhotoInput` з coin-form.tsx (без «Покращити»): кругле превʼю, тап — вибір фото.
@Composable
private fun PhotoInput(label: String, preview: String?, onPick: (Uri) -> Unit) {
    // ≈ `<input type="file" accept="image/*">`: системний Photo Picker (дозвіл на галерею не потрібен).
    // `rememberLauncherForActivityResult` — відкрити інший екран системи і отримати результат у колбек.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        // null — picker закрили, нічого не вибравши.
        picked?.let(onPick)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(contentAlignment = Alignment.Center) {
            // Coil показує і content:// з галереї, не лише URL (≈ `URL.createObjectURL(file)` для превʼю).
            CoinPhoto(
                preview,
                120.dp,
                Modifier.clickable {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )
            if (preview == null) Text("+", fontSize = 32.sp)
        }
        Text(label)
    }
}
