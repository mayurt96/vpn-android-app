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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bunnyvpn.model.VpnState
import com.bunnyvpn.ui.components.*
import com.bunnyvpn.ui.screens.formatSpeed
import com.bunnyvpn.ui.screens.formatTimer
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
    val networkStats by viewModel.networkStats.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val timer by viewModel.connectionTimer.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()

    // Animated bars
    val downloadAnim by animateFloatAsState(
        targetValue = (networkStats.downloadSpeed / 500f).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "dl"
    )
    val uploadAnim by animateFloatAsState(
        targetValue = (networkStats.uploadSpeed / 200f).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "ul"
    )

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
                "Statistics",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )
            Text("Live network analytics", color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(20.dp))

            // Connection status card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("CONNECTION STATUS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    when (vpnState) {
                                        VpnState.CONNECTED -> StatusConnected
                                        VpnState.CONNECTING -> StatusConnecting
                                        VpnState.DISCONNECTED -> StatusDisconnected
                                    },
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(
                            when (vpnState) {
                                VpnState.CONNECTED -> "Connected"
                                VpnState.CONNECTING -> "Connecting"
                                VpnState.DISCONNECTED -> "Disconnected"
                            },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (vpnState == VpnState.CONNECTED) {
                        Text(formatTimer(timer), color = StatusConnected, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (vpnState == VpnState.CONNECTED) {
                    Spacer(Modifier.height(8.dp))
                    StatRow("Server", "${selectedServer.flag} ${selectedServer.country}")
                    Spacer(Modifier.height(4.dp))
                    StatRow("VPN IP", deviceInfo.fakeIp)
                    Spacer(Modifier.height(4.dp))
                    StatRow("Ping", "${selectedServer.ping} ms")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Speed bars
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("LIVE SPEED")

                SpeedBar(
                    label = "↓ Download",
                    value = networkStats.downloadSpeed,
                    progress = downloadAnim,
                    color = Cyan400
                )
                Spacer(Modifier.height(14.dp))
                SpeedBar(
                    label = "↑ Upload",
                    value = networkStats.uploadSpeed,
                    progress = uploadAnim,
                    color = Purple400
                )
            }

            Spacer(Modifier.height(12.dp))

            // Data usage
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("DATA USAGE")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AnimatedCounter(
                        value = formatBytes(networkStats.downloadTotal),
                        label = "Downloaded",
                        valueColor = Cyan400
                    )
                    Box(Modifier.width(1.dp).height(40.dp).background(GlassBorder))
                    AnimatedCounter(
                        value = formatBytes(networkStats.uploadTotal),
                        label = "Uploaded",
                        valueColor = Purple400
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // IP Info
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("IP INFORMATION")
                StatRow("Public IP", deviceInfo.realIp)
                Spacer(Modifier.height(6.dp))
                StatRow("Operator", deviceInfo.simOperator)
                Spacer(Modifier.height(6.dp))
                StatRow("Network", deviceInfo.networkType)
                Spacer(Modifier.height(6.dp))
                StatRow("Location", deviceInfo.city)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun SpeedBar(label: String, value: Float, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(formatSpeed(value), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(GlassWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color)))
            )
        }
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "${"%.2f".format(bytes / 1_073_741_824f)} GB"
        bytes >= 1_048_576 -> "${"%.1f".format(bytes / 1_048_576f)} MB"
        bytes >= 1024 -> "${"%.0f".format(bytes / 1024f)} KB"
        else -> "$bytes B"
    }
}
