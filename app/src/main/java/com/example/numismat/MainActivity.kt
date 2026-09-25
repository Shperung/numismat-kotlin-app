// Пакет — "адреса" файлу в проєкті (як шлях до модуля в TS). Має збігатися з папками.
package com.example.numismat

// Імпорти — як `import { Text } from 'react-native'`.
// Android Studio додає їх автоматично (Option+Enter на червоному слові).
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.numismat.ui.theme.NumismatTheme

// Activity — точка входу додатку (аналог index.js + AppRegistry.registerComponent у RN).
// `: ComponentActivity()` — наслідування, як `extends` у TS.
// Саме цей клас вказаний у AndroidManifest.xml як той, що запускається з ярлика.
class MainActivity : ComponentActivity() {

    // onCreate — викликається системою при старті екрана (схоже на componentDidMount / перший рендер).
    // `override` — обов'язкове слово, коли перевизначаємо метод батьківського класу.
    // `Bundle?` — знак `?` означає "може бути null" (як `Bundle | null` у TS).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Малювати під статус-баром і навігаційною панеллю (edge-to-edge),
        // відступи потім беремо з innerPadding нижче — як SafeAreaView у RN.
        enableEdgeToEdge()

        // setContent { ... } — "корінь" Compose UI, як <App /> у RN.
        // Фігурні дужки після функції — це лямбда (аналог () => { ... }).
        setContent {
            // Тема (кольори, шрифти) — як ThemeProvider / Context.Provider.
            NumismatTheme {
                // Кореневий навігатор з табами — як src/app/_layout.tsx в Expo Router.
                TabLayout()
            }
        }
    }
}

// @Preview — показує компонент у панелі Design/Split в Android Studio без запуску на телефоні
// (щось на кшталт Storybook). На сам додаток не впливає.
@Preview(showBackground = true)
@Composable
fun TabLayoutPreview() {
    NumismatTheme {
        TabLayout()
    }
}
