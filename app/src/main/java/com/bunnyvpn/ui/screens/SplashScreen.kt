package com.bunnyvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.bunnyvpn.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: (Boolean) -> Unit, isLoggedIn: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        onFinished(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Purple400.copy(alpha = 0.2f),
                        BgBlack
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Glow ring
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .alpha(glow * 0.4f)
                            .background(
                                Brush.radialGradient(listOf(Cyan400, Purple400, Color.Transparent)),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(glow)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "BunnyVPN",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                    )
                )
                Text(
                    text = "SECURE  •  FAST  •  PRIVATE",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(48.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Cyan400,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
