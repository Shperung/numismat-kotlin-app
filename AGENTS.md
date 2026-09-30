# Numismat — персональна колекція монет (Kotlin / Android)

## Мета проєкту
Навчальний проєкт: вивчити Kotlin з нуля, будуючи Android-додаток для обліку
особистої колекції монет. Починаємо від Hello World і рухаємося малими кроками.

## Про автора
- Великий досвід у React Native / TypeScript.
- Kotlin та нативний Android не знає — вчить по ходу.

## Як працюємо (правила для AI)
- НЕ генерувати готовий проєкт чи великі шматки коду за раз — лише малі кроки.
- Кожну нову концепцію пояснювати через аналогію з React Native / TypeScript.
- Код мінімальний і простий, без передчасних абстракцій та зайвих бібліотек.
- Перед новою темою коротко пояснити "навіщо", потім "як".
- Мова спілкування — українська.
- Не запускати збірку (`gradlew`) і додаток — автор запускає сам.
- Після завершення кроку оновлювати розділи "План", "Поточний стан" і "Журнал" у цьому файлі.

## Функціональність додатку (цільова)
- Список монет колекції.
- Додавання / редагування / видалення монети.
- Поля монети: назва, країна, рік, номінал, метал, стан (grade), ціна покупки,
  дата придбання, нотатки, фото аверсу та реверсу.
- Деталі монети на окремому екрані.
- Пошук і фільтри (країна, рік, метал).
- Статистика колекції (кількість, загальна вартість, розподіл за країнами).
- Локальне збереження даних (офлайн, без бекенду).
- Можливо пізніше: експорт/імпорт, інтеграція з каталогом Numista API.

## Стек
- Мова: Kotlin
- UI: Jetpack Compose (Material 3)
- Навігація: Navigation Compose
- Стан / логіка: ViewModel + StateFlow, корутини
- База даних: Room
- Зображення: Coil
- Збірка: Gradle (Kotlin DSL, `build.gradle.kts`)
- IDE: Android Studio
- Тестовий пристрій: Samsung S25 Ultra (Android 16 / API 36), запасний варіант — емулятор

## Шпаргалка React Native → Kotlin/Android
| React Native            | Kotlin / Android                 |
|-------------------------|----------------------------------|
| Функціональний компонент | `@Composable` функція            |
| `useState`              | `remember { mutableStateOf() }`  |
| `useEffect`             | `LaunchedEffect`                 |
| `FlatList`              | `LazyColumn`                     |
| `style` / flexbox       | `Modifier`, `Column`/`Row`/`Box` |
| React Navigation        | Navigation Compose               |
| Redux / Zustand         | `ViewModel` + `StateFlow`        |
| `async/await`           | корутини, `suspend`              |
| SQLite / AsyncStorage   | Room / DataStore                 |
| `package.json`          | `build.gradle.kts`               |

## План
0. [x] Середовище: Android Studio + S25 Ultra (USB debugging) / емулятор
1. [ ] Основи Kotlin: val/var, null-safety, data class, лямбди, колекції
2. [ ] Hello World на Compose, розбір структури проєкту
3. [ ] Основи Compose: верстка, Modifier, стан (лічильник)
4. [ ] Модель `Coin` + список монет із захардкодженими даними (LazyColumn)
5. [ ] Форма додавання монети, state hoisting
6. [ ] Навігація: Список → Деталі → Додати
7. [ ] ViewModel + StateFlow
8. [ ] Room: збереження між запусками, корутини
9. [ ] Фото монет (камера/галерея) + Coil
10. [ ] Пошук, фільтри, статистика

## Поточний стан
Крок 2 — проєкт створено з шаблону Empty Activity (Compose), package `com.example.numismat`.
Hello World запущено на S25 Ultra. Додано bottom tabs (Головна / Список / Інфо) на `HorizontalPager`,
кожен таб — порожній екран з назвою (як в Expo-версії); стан табів не губиться при перемиканні.
Крок 1 (основи Kotlin) поки пропущено — пояснюємо синтаксис по ходу.
Підключено Firestore (як в Expo-версії): колекція `coins` читається через `CoinsViewModel`.
Країни — спільний `CountriesViewModel` (як `CountriesProvider` в Expo). Головна — випадкова монета випадкової країни
(`CoinDetails`). «Список» — фільтр за країною: dropdown, при старті випадкова країна, монети через `whereEqualTo`,
тап по `CoinCard` відкриває екран монети (стек поверх табів, як в Expo).
У `CoinDetails` — кнопка «Дізнатись цікаві факти» (Firebase AI Logic, Gemini) — працює на S25 Ultra, поки одна відповідь без чату, markdown не рендериться.

## Журнал (що вивчено / зроблено)
- Встановлено Android Studio, підключено S25 Ultra (USB debugging, вимкнено Auto Blocker).
- Створено проєкт Numismat (Kotlin DSL, Compose), Hello World працює на телефоні.
- Розібрано `MainActivity.kt`: Activity, onCreate, setContent, @Composable, Scaffold, Modifier, @Preview
  (у коді є коментарі українською).
- Іконка додатку: векторна adaptive icon з монетками (`drawable/ic_launcher_*.xml`).
- Build types: debug vs release (`buildTypes` в `app/build.gradle.kts`, панель Build Variants).
- Bottom tabs: `TabLayout.kt` (аналог Expo `_layout.tsx`: `Scaffold` + `TopAppBar` + `NavigationBar` + `HorizontalPager`),
 екрани в `screens/` (`HomeScreen`, `ListScreen`, `InfoScreen`). Залежності: `navigation-compose`, `material-icons-core`.
- Стан табів: `NavHost` знищує екран при перемиканні (`remember` губиться), тому для табів — `HorizontalPager`
 з `beyondViewportPageCount` (всі таби живуть, як у RN Tabs / SwiftUI TabView), свайп вимкнено,
 "Назад" → на "Головна" (`BackHandler`). `navigation-compose` лишається для стеку Список → Деталі (крок 6).
- Firebase Firestore (спільний проєкт з Expo-версією): конфіг у `local.properties` (`FIREBASE_*`, аналог `.env.local`)
 → `BuildConfig` (аналог `EXPO_PUBLIC_*`); ініціалізація з коду через `FirebaseOptions` (без `google-services.json`).
 `lib/Firebase.kt` (`initFirebase` у `MainActivity.onCreate`, `db`), `lib/FetchCollection.kt` (`suspend` + `.await()`),
 `CoinsViewModel.kt` (`StateFlow`, аналог Context-провайдера), `HomeScreen` — `viewModel()` + `collectAsState()`, JSON через `JSONArray`.
 Залежності: `firebase-bom` + `firebase-firestore`, `kotlinx-coroutines-play-services`, `lifecycle-viewmodel-compose`.
- `CountriesViewModel` + `ListScreen` — колекція `countries` як JSON (зроблено самостійно по аналогії).
- Картки та екран монети: `model/Coin.kt` (`data class` + `coinFromMap`, `as?`), `components/CoinCard.kt`
 (`Card(onClick)`, `CoinPhoto` через Coil `AsyncImage`), `HomeScreen` — `LazyColumn` + `items(key)`,
 `screens/CoinScreen.kt` (`Scaffold` + `TopAppBar` зі стрілкою "Назад").
 `RootLayout.kt` — `NavHost` ("tabs" → `TabLayout`, "coin/{id}" → `CoinScreen`), аналог кореневого `Stack`.
 `viewModel()` всередині `composable {}` скоупиться на екран стеку, тому `CoinsViewModel` береться в `RootLayout`
 вище за `NavHost`, а стан передається параметрами (state hoisting). Залежності: `coil-compose`, `coil-network-okhttp`.
- Coil 3.6.x потребує Kotlin 2.4 (`kotlin-stdlib` 2.4.10) → з компілятором 2.2.10 падало `Unresolved reference 'emptyList'`.
 Тому Coil 3.3.0 (stdlib 2.2.0). Новіший Coil — лише після підняття версії `kotlin`.
- Фільтр за країною: `model/Country.kt` (`name_ua` → `nameUa`), `lib/FetchCoinsByCountry.kt` (`whereEqualTo`),
 `CountriesViewModel` (`copy`, `_state.update {}`, `randomOrNull()`; попередній запит скасовується через `Job.cancel()`
 замість прапорця `active` з Expo; `CancellationException` не ловимо як помилку). Логіка у ViewModel, а не в `remember`,
 бо при переході на екран монети таби виходять з композиції. `ListScreen` — `LazyColumn` з `item {}` (header / empty),
 `CountryPicker` на `ExposedDropdownMenuBox` (+ перший `remember { mutableStateOf() }` для `expanded`).
- Випадкова монета на Головній (як коміт "add random coin" в Expo): поле `year` у `Coin`, `components/CoinDetails.kt`
 (спільний для Головної і `CoinScreen`, параметр `modifier: Modifier = Modifier`). `CountriesViewModel` тепер лише
 довідник країн (у `RootLayout`, передається в таби параметром), логіка фільтра — у `ListViewModel`,
 випадкова монета — у `HomeViewModel`. `LaunchedEffect(countries)` ≈ `useEffect(..., [countries])`,
 ViewModel сам стежить, щоб вибір робився один раз. `randomOrNull()` замість `pickRandom`, `?: return`,
 smart cast через локальну `val` (з делегатом `by` не працює).
- AI-факти про монету (як коміт "add ai feature" в Expo): `firebase-ai` з BOM (17.17.0, stdlib 2.0.21 — сумісно).
 `lib/Ai.kt` — `startCoinChat(coin)`: `Firebase.ai(backend = GenerativeBackend.agentPlatform("global"))`
 (`vertexAI` — deprecated), модель `gemini-3.5-flash-lite`, `systemInstruction = content { text(...) }`, `.startChat()`.
 У `CoinDetails` — `remember { mutableStateOf() }` для answer/loading, `rememberCoroutineScope().launch` для запиту,
 `Button` + `CircularProgressIndicator`. Налаштування консолі (App Check Unenforced для AI Logic, Cloud Billing) —
 спільні з Expo-версією, бо проєкт Firebase той самий.
 На емуляторі (робочий Mac) — `UnknownException`, причина в `e.cause`: `UnknownHostException firebasevertexai.googleapis.com`
 (мережа/DNS емулятора; Firestore при цьому "працює" з офлайн-кешу). На S25 Ultra — ок. Для діагностики в `catch`
 лишено `Log.e("AI", ...)` + `e.cause` у тексті помилки.
