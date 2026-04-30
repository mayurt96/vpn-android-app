package com.bunnyvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
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
import com.bunnyvpn.ui.components.*
import com.bunnyvpn.ui.screens.VpnTextField
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        editName = profile.name
        editEmail = profile.email
        editPhone = profile.phone
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )

            Spacer(Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(listOf(Cyan400.copy(0.3f), Purple400.copy(0.2f))),
                        CircleShape
                    )
                    .border(2.dp, Brush.sweepGradient(listOf(Cyan400, Purple400, Cyan400)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.name.firstOrNull()?.uppercase() ?: "B",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                    )
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(profile.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(profile.email, color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))

            // Edit / View Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("PERSONAL INFO")
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                viewModel.updateProfile(editName, editEmail, editPhone)
                            }
                            isEditing = !isEditing
                        }
                    ) {
                        Icon(
                            if (isEditing) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                AnimatedContent(targetState = isEditing, label = "edit_anim") { editing ->
                    if (editing) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            VpnTextField(editName, { editName = it }, "Name", Icons.Filled.Person)
                            VpnTextField(editEmail, { editEmail = it }, "Email", Icons.Filled.Email)
                            VpnTextField(editPhone, { editPhone = it }, "Phone", Icons.Filled.Phone)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileInfoRow(Icons.Filled.Person, "Name", profile.name)
                            ProfileInfoRow(Icons.Filled.Email, "Email", profile.email)
                            ProfileInfoRow(Icons.Filled.Phone, "Phone", profile.phone.ifBlank { "Not set" })
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Device Info Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("DEVICE & NETWORK")
                ProfileInfoRow(Icons.Filled.Wifi, "IP Address", deviceInfo.realIp)
                Spacer(Modifier.height(6.dp))
                ProfileInfoRow(Icons.Filled.SimCard, "SIM Operator", deviceInfo.simOperator)
                Spacer(Modifier.height(6.dp))
                ProfileInfoRow(Icons.Filled.NetworkCell, "Network Type", deviceInfo.networkType)
                Spacer(Modifier.height(6.dp))
                ProfileInfoRow(Icons.Filled.LocationOn, "City", deviceInfo.city)
                Spacer(Modifier.height(6.dp))
                ProfileInfoRow(Icons.Filled.Home, "Address", deviceInfo.fullAddress)
            }

            Spacer(Modifier.height(20.dp))

            // Logout
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusDisconnected.copy(0.15f))
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = StatusDisconnected, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = StatusDisconnected, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Cyan400.copy(0.1f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
            Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
