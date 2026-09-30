package com.example.numismat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

// Аналог src/app/photo.tsx: фото на весь екран з pinch zoom.
// В Expo це окремий роут з presentation: 'fullScreenModal', а URL передається параметром (звідси морока з encodeURIComponent).
// Тут — Dialog (≈ <Modal> у RN): показується поверх усього, URL передаємо звичайним параметром,
// а системна кнопка/жест "Назад" сама викликає onDismissRequest.
@Composable
fun PhotoViewer(uri: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        // usePlatformDefaultWidth = false — не звичайне вікно діалогу посередині, а на весь екран;
        // decorFitsSystemWindows = false — малювати і під статус-баром (edge-to-edge, як у MainActivity).
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val scope = rememberCoroutineScope()
        // Animatable ≈ useSharedValue: значення, яке можна змінити миттєво (snapTo ≈ `.value = ...`)
        // або плавно (animateTo ≈ withTiming). Offset.VectorConverter — щоб анімувати пару x/y разом.
        val scale = remember { Animatable(1f) }
        val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

        // Box ≈ View, у якому діти накладаються один на одного (як position: 'absolute').
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                // ≈ contentFit="contain".
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    // pointerInput — "сирі" дотики, всередині — готові детектори жестів (≈ GestureDetector).
                    // detectTransformGestures — pinch і pan одночасно (≈ Gesture.Simultaneous(pinch, pan)).
                    // На відміну від Expo, zoom і pan приходять як зміна з минулого кадру, а не від початку жесту,
                    // тому savedScale / savedX / savedY не потрібні — просто множимо/додаємо до поточного.
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                // coerceIn(1f, 5f) ≈ Math.min(5, Math.max(1, ...)).
                                scale.snapTo((scale.value * zoom).coerceIn(1f, 5f))
                                // Як в Expo: зсувати фото можна лише коли воно збільшене.
                                if (scale.value > 1f) offset.snapTo(offset.value + pan)
                            }
                        }
                    }
                    // Окремий pointerInput для подвійного тапу — скинути зум (≈ Gesture.Tap().numberOfTaps(2)).
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Дві корутини — щоб масштаб і зсув анімувались одночасно, а не по черзі.
                                scope.launch { scale.animateTo(1f) }
                                scope.launch { offset.animateTo(Offset.Zero) }
                            },
                        )
                    }
                    // graphicsLayer ≈ useAnimatedStyle({ transform: [...] }): зміна лише "картинки" без перерахунку верстки,
                    // тому це дешево робити щокадру під час жесту.
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offset.value.x
                        translationY = offset.value.y
                    },
            )
            // Кнопка закриття у правому верхньому куті (≈ position: 'absolute', top: insets.top + 8, right: 16).
            // statusBarsPadding() ≈ insets.top з useSafeAreaInsets().
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 16.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Закрити")
            }
        }
    }
}
