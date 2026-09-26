import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Аналог `.env.local` в Expo: читаємо local.properties (він у .gitignore).
val localProps = Properties().apply {
    rootProject.file("local.properties").inputStream().use { load(it) }
}

android {
    namespace = "com.example.numismat"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.numismat"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Аналог префікса EXPO_PUBLIC_: значення вшиваються в код під час збірки
        // і доступні як BuildConfig.FIREBASE_API_KEY (≈ process.env.EXPO_PUBLIC_FIREBASE_API_KEY).
        listOf("API_KEY", "PROJECT_ID", "STORAGE_BUCKET", "APP_ID").forEach { key ->
            buildConfigField("String", "FIREBASE_$key", "\"${localProps.getProperty("FIREBASE_$key")}\"")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // Генерує клас BuildConfig з полями вище.
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Аналог `npx expo install expo-router` — навігація.
    implementation(libs.androidx.navigation.compose)
    // Аналог `@expo/vector-icons` — набір іконок Material.
    implementation(libs.androidx.compose.material.icons.core)
    // Аналог `npm i firebase` — BOM (bill of materials) сам підбирає сумісні версії модулів Firebase.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    // Дає `.await()` для Firebase Task — щоб писати `getDocs(...)` як `await` без колбеків.
    implementation(libs.kotlinx.coroutines.play.services)
    // `viewModel()` у Compose — замість Context Provider (див. CoinsViewModel.kt).
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}