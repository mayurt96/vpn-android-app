package com.bunnyvpn.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.bunnyvpn.ui.theme.*

// ---- Glass Card ----
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val borderColor = if (theme == AppTheme.NEON) NeonPink.copy(alpha = 0.4f) else GlassBorder
    val bgColor = if (theme == AppTheme.LIGHT) LightCard else GlassWhite

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, Color.Transparent, borderColor)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

// ---- Neon Glow Button ----
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    // Simplified button for better performance in emulator
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Cyan400, Purple400))
                else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// ---- Animated Counter ----
@Composable
fun AnimatedCounter(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Cyan400
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
            },
            label = "counter_anim"
        ) { v ->
            Text(
                text = v,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---- Stat Row ----
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ---- Ping Badge ----
@Composable
fun PingBadge(ping: Int) {
    val color = when {
        ping < 50 -> StatusConnected
        ping < 150 -> StatusConnecting
        else -> StatusDisconnected
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = "${ping}ms", color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---- Section Header ----
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = Cyan400,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// ---- Animated Gradient Background ----
@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    // Simplified background to reduce lag and fix visibility issues in emulator
    val theme = LocalAppTheme.current
    val bgColor = when (theme) {
        AppTheme.NEON -> Color(0xFF000010)
        AppTheme.LIGHT -> LightBg
        else -> BgBlack
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    )
}
