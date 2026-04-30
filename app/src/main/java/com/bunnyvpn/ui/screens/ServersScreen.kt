package com.bunnyvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bunnyvpn.model.DummyServers
import com.bunnyvpn.model.VpnServer
import com.bunnyvpn.ui.components.*
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel

@Composable
fun ServersScreen(viewModel: MainViewModel) {
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(
                "VPN Servers",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )
            Text("${DummyServers.size} locations available", color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(DummyServers, key = { it.id }) { server ->
                    ServerCard(
                        server = server,
                        isSelected = server.id == selectedServer.id,
                        onClick = { viewModel.selectServer(server) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ServerCard(server: VpnServer, isSelected: Boolean, onClick: () -> Unit) {
    val borderBrush = if (isSelected)
        Brush.linearGradient(listOf(Cyan400, Purple400))
    else
        Brush.linearGradient(listOf(GlassBorder, GlassBorder))

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Cyan400.copy(0.08f) else GlassWhite)
            .border(1.5.dp, borderBrush, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(server.flag, fontSize = 28.sp)
            Column {
                Text(server.country, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(server.city, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                PingBadge(server.ping)
                Spacer(Modifier.height(4.dp))
                Text("${server.load}% load", color = TextMuted, fontSize = 10.sp)
            }
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
            }
        }
    }
}
