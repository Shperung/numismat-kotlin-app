// Пакет — "адреса" файлу в проєкті (як шлях до модуля в TS). Має збігатися з папками.
package com.example.numismat

// Імпорти — як `import { Text } from 'react-native'`.
// Android Studio додає їх автоматично (Option+Enter на червоному слові).
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.numismat.ui.theme.NumismatTheme
import com.example.numismat.ui.theme.PurpleGrey40

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
                // Scaffold — готовий "каркас" екрана Material 3:
                // місце для TopBar, BottomBar, FAB і основного контенту.
                // Modifier.fillMaxSize() — як style={{ flex: 1 }}.
                // `innerPadding ->` — параметр лямбди: відступи від системних панелей.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PurpleGrey40
                ) { innerPadding ->
                    // Виклик нашого компонента з "пропсами".
                    // `name = "Android"` — іменований аргумент (як { name: 'Android' }).
                    Greeting(
                        name = "world",
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color.Gray)

                    )
                }
            }
        }
    }
}

// @Composable — це "компонент". Аналог:
//   const Greeting = ({ name, modifier }: { name: string; modifier?: Modifier }) => ...
// `fun` — оголошення функції. Тип пишеться ПІСЛЯ імені: `name: String`.
// `modifier: Modifier = Modifier` — параметр зі значенням за замовчуванням (як `modifier = {}`).
// Composable нічого не повертає (немає return JSX) — вона просто "викликає" інші компоненти.
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        // "$name" — шаблонний рядок, як `Hello ${name}!` у JS.
        text = "Hello $name!",
        // Modifier — стилі та поведінка (відступи, розмір, клік). Прийнято передавати його ззовні,
        // щоб батько міг керувати розміщенням — як проп `style` у RN.
        color = Color.Red,
        modifier = modifier
    )
}

// @Preview — показує компонент у панелі Design/Split в Android Studio без запуску на телефоні
// (щось на кшталт Storybook). На сам додаток не впливає.
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NumismatTheme {
        Greeting("Android")
    }
}
