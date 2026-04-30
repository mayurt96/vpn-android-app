package com.bunnyvpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bunnyvpn.ui.components.*
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val autoConnect by viewModel.autoConnect.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )
            Text("Customize your experience", color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))

            // Theme Selection
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("APPEARANCE")
                Text("Theme", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeChip(
                        label = "🌑 Dark",
                        selected = appTheme == AppTheme.DARK,
                        onClick = { viewModel.setTheme(AppTheme.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = "☀️ Light",
                        selected = appTheme == AppTheme.LIGHT,
                        onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = "⚡ Neon",
                        selected = appTheme == AppTheme.NEON,
                        onClick = { viewModel.setTheme(AppTheme.NEON) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Connection Settings
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("CONNECTION")
                SettingsToggleRow(
                    icon = Icons.Filled.FlashOn,
                    title = "Auto Connect",
                    subtitle = "Connect VPN on app launch",
                    checked = autoConnect,
                    onCheckedChange = { viewModel.setAutoConnect(it) }
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = GlassBorder)
                Spacer(Modifier.height(12.dp))
                SettingsToggleRow(
                    icon = Icons.Filled.Security,
                    title = "Kill Switch",
                    subtitle = "Block traffic if VPN drops (UI only)",
                    checked = false,
                    onCheckedChange = {}
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = GlassBorder)
                Spacer(Modifier.height(12.dp))
                SettingsToggleRow(
                    icon = Icons.Filled.Dns,
                    title = "DNS Leak Protection",
                    subtitle = "Use secure DNS servers (UI only)",
                    checked = true,
                    onCheckedChange = {}
                )
            }

            Spacer(Modifier.height(12.dp))

            // Privacy
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("PRIVACY")
                SettingsToggleRow(
                    icon = Icons.Filled.VisibilityOff,
                    title = "No-Log Policy",
                    subtitle = "We never store your activity",
                    checked = true,
                    onCheckedChange = {}
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = GlassBorder)
                Spacer(Modifier.height(12.dp))
                SettingsToggleRow(
                    icon = Icons.Filled.Block,
                    title = "Ad Blocker",
                    subtitle = "Block ads & trackers (UI only)",
                    checked = false,
                    onCheckedChange = {}
                )
            }

            Spacer(Modifier.height(12.dp))

            // App Info
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("ABOUT")
                SettingsInfoRow(icon = Icons.Filled.Info, title = "Version", value = "1.0.0")
                Spacer(Modifier.height(10.dp))
                Divider(color = GlassBorder)
                Spacer(Modifier.height(10.dp))
                SettingsInfoRow(icon = Icons.Filled.Code, title = "Developer", value = "Bunny")
                Spacer(Modifier.height(10.dp))
                Divider(color = GlassBorder)
                Spacer(Modifier.height(10.dp))
                SettingsInfoRow(icon = Icons.Filled.Shield, title = "Protocol", value = "WireGuard (Demo)")
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Brush.linearGradient(listOf(Cyan400.copy(0.2f), Purple400.copy(0.2f)))
                else Brush.linearGradient(listOf(GlassWhite, GlassWhite))
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                brush = if (selected) Brush.linearGradient(listOf(Cyan400, Purple400))
                else Brush.linearGradient(listOf(GlassBorder, GlassBorder)),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Cyan400 else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Cyan400.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Cyan400,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = GlassWhite
            )
        )
    }
}

@Composable
fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Purple400.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Purple400, modifier = Modifier.size(18.dp))
            }
            Text(title, color = TextPrimary, fontSize = 14.sp)
        }
        Text(value, color = TextSecondary, fontSize = 13.sp)
    }
}
