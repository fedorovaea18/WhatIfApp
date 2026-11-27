package ru.eafedorova.whatifapp.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.R

@Composable
fun SplashScreen(
    darkTheme: Boolean,
    onAnimationFinished: () -> Unit
) {
    val bgColor = colorResource(
        if (darkTheme) R.color.splash_bg_dark else R.color.splash_bg_light
    )
    val textColor = colorResource(
        if (darkTheme) R.color.splash_text_dark else R.color.splash_text_light
    )
    val secondaryTextColor = colorResource(
        if (darkTheme) R.color.splash_text_secondary_dark else R.color.splash_text_secondary_light
    )
    val logoRes = if (darkTheme) {
        R.drawable.ic_splash_logo_dark
    } else {
        R.drawable.ic_splash_logo_light
    }

    val logoScale = remember { Animatable(1f) }
    val logoOffsetY = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(8f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 300
                    1f at 0
                    1.03f at 150
                    1f at 300
                }
            )
        }
        launch {
            logoOffsetY.animateTo(
                targetValue = -20f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(durationMillis = 300))
            titleOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
        }

        delay(300)
        delay(100)
        subtitleAlpha.animateTo(1f, animationSpec = tween(durationMillis = 200))

        delay(450)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        translationY = logoOffsetY.value
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Что если?",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = textColor,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffsetY.value
                    }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Место для твоих мыслей",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = secondaryTextColor.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                }
            )
        }
    }
}