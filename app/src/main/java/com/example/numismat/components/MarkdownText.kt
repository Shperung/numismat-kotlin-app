package com.example.numismat.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography

// Аналог src/components/markdown-text.tsx: текст відповіді AI з markdown (**жирний**, списки, заголовки).
// Бібліотека за замовчуванням бере для h1 найбільший стиль теми (displayLarge, ~57sp) — для картки завелико.
// Тому, як `styles: { h1: { fontSize: 20 }, ... }` в Expo, зменшуємо заголовки до стилів "title" з теми.
@Composable
fun MarkdownText(value: String, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    Markdown(
        content = value,
        modifier = modifier,
        typography = markdownTypography(
            h1 = typography.titleLarge,
            h2 = typography.titleMedium,
            h3 = typography.titleSmall,
            h4 = typography.titleSmall,
            h5 = typography.bodyLarge,
            h6 = typography.bodyLarge,
        ),
    )
}
