package com.bunnyvpn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bunnyvpn.model.VpnState
import com.bunnyvpn.ui.components.*
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val networkStats by viewModel.networkStats.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val timer by viewModel.connectionTimer.collectAsStateWithLifecycle()

    // Helper: fetch location using FusedLocationProvider with callback fallback
    fun fetchLocation(onGranted: (() -> Unit)? = null) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModel.updateLocation(context, loc.latitude, loc.longitude)
                } else {
                    // lastLocation null — request fresh fix
                    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                        .setMaxUpdates(1)
                        .build()
                    fusedClient.requestLocationUpdates(req, object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { fresh ->
                                viewModel.updateLocation(context, fresh.latitude, fresh.longitude)
                            }
                            fusedClient.removeLocationUpdates(this)
                        }
                    }, Looper.getMainLooper())
                }
            }.addOnFailureListener {
                // permissions not ready yet — ignore
            }
        } catch (_: SecurityException) {}
        onGranted?.invoke()
    }

    // Location permission launcher
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation()
    }

    // Phone state permission launcher (for SIM operator)
    val phonePermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadSimInfo(context)
    }

    LaunchedEffect(Unit) {
        // Check & request READ_PHONE_STATE for SIM info
        val hasPhone = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
        if (hasPhone) {
            viewModel.loadSimInfo(context)
        } else {
            phonePermLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }

        // Check & request Location
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchLocation()
        } else {
            locationPermLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BunnyVPN", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                        )
                    )
                    Text(
                        when (vpnState) {
                            VpnState.DISCONNECTED -> "Not Protected"
                            VpnState.CONNECTING -> "Connecting..."
                            VpnState.CONNECTED -> "Protected ✓"
                        },
                        color = when (vpnState) {
                            VpnState.DISCONNECTED -> StatusDisconnected
                            VpnState.CONNECTING -> StatusConnecting
                            VpnState.CONNECTED -> StatusConnected
                        },
                        fontSize = 12.sp
                    )
                }
                // Server chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(selectedServer.flag, fontSize = 16.sp)
                    Text(selectedServer.city, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))

            // === CONNECT BUTTON ===
            ConnectButton(vpnState = vpnState, onToggle = { viewModel.toggleVpn() })

            // Timer
            AnimatedVisibility(visible = vpnState == VpnState.CONNECTED) {
                Text(
                    text = formatTimer(timer),
                    color = StatusConnected,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // === IP CARD ===
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("IP ADDRESS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Real IP", color = TextSecondary, fontSize = 11.sp)
                        Text(deviceInfo.realIp, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("VPN IP", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            if (vpnState == VpnState.CONNECTED) deviceInfo.fakeIp else "---",
                            color = if (vpnState == VpnState.CONNECTED) StatusConnected else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // === SPEED CARD ===
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("NETWORK SPEED")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AnimatedCounter(
                        value = "↓ ${formatSpeed(networkStats.downloadSpeed)}",
                        label = "Download",
                        valueColor = Cyan400
                    )
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(GlassBorder)
                    )
                    AnimatedCounter(
                        value = "↑ ${formatSpeed(networkStats.uploadSpeed)}",
                        label = "Upload",
                        valueColor = Purple400
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // === MAP ===
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("LOCATION")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null,
                        tint = Cyan400, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(deviceInfo.city, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(deviceInfo.fullAddress, color = TextSecondary, fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 10.dp))

                OSMMapView(
                    latitude = deviceInfo.latitude,
                    longitude = deviceInfo.longitude,
                    label = deviceInfo.city.ifBlank { "Your Location" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(Modifier.height(12.dp))

            // === SIM INFO ===
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("DEVICE INFO")
                StatRow("SIM Operator", deviceInfo.simOperator)
                Spacer(Modifier.height(6.dp))
                StatRow("Network Type", deviceInfo.networkType)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ConnectButton(vpnState: VpnState, onToggle: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "connect_btn")

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState == VpnState.DISCONNECTED) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "breathe"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rotate"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseOut), RepeatMode.Restart),
        label = "ripple"
    )

    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseOut), RepeatMode.Restart),
        label = "ripple_r"
    )

    val buttonColor = when (vpnState) {
        VpnState.DISCONNECTED -> StatusDisconnected
        VpnState.CONNECTING -> StatusConnecting
        VpnState.CONNECTED -> StatusConnected
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Ripple (connected only)
        if (vpnState == VpnState.CONNECTED) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = StatusConnected,
                    radius = rippleRadius,
                    alpha = rippleAlpha,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }

        // Outer glow ring
        Box(
            modifier = Modifier
                .size(175.dp)
                .scale(buttonScale)
                .background(
                    Brush.radialGradient(
                        listOf(buttonColor.copy(0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Rotating loader ring (connecting only)
        if (vpnState == VpnState.CONNECTING) {
            Canvas(
                modifier = Modifier
                    .size(155.dp)
                    .rotate(rotationAngle)
            ) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(Cyan400, Purple400, Color.Transparent)),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )
            }
        }

        // Main button
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(buttonScale)
                .background(
                    Brush.radialGradient(
                        listOf(
                            buttonColor.copy(0.35f),
                            BgCard
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    3.dp,
                    Brush.sweepGradient(listOf(buttonColor, buttonColor.copy(0.3f), buttonColor)),
                    CircleShape
                )
                .clickable { if (vpnState != VpnState.CONNECTING) onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Power,
                    contentDescription = null,
                    tint = buttonColor,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    when (vpnState) {
                        VpnState.DISCONNECTED -> "CONNECT"
                        VpnState.CONNECTING -> "..."
                        VpnState.CONNECTED -> "ON"
                    },
                    color = buttonColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

fun formatTimer(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

fun formatSpeed(kbps: Float): String {
    return if (kbps >= 1024) "${"%.1f".format(kbps / 1024)} MB/s"
    else "${"%.0f".format(kbps)} KB/s"
}

// ---- OpenStreetMap Composable — FREE, no API key ----
@Composable
fun OSMMapView(
    latitude: Double,
    longitude: Double,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // OSMdroid config — must set user agent
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.cacheDir
            osmdroidTileCache = java.io.File(context.cacheDir, "osm_tiles")
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // Use standard OSM tile source
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false

                // Dark overlay filter via color matrix
                overlayManager.tilesOverlay.setColorFilter(
                    android.graphics.ColorMatrixColorFilter(
                        floatArrayOf(
                            -1f,  0f,  0f, 0f, 255f,  // Red   inverted
                             0f, -1f,  0f, 0f, 255f,  // Green inverted
                             0f,  0f, -1f, 0f, 255f,  // Blue  inverted
                             0f,  0f,  0f, 1f,   0f   // Alpha unchanged
                        )
                    )
                )

                // Initial position
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(latitude, longitude))

                // Add marker
                val marker = Marker(this).apply {
                    position = GeoPoint(latitude, longitude)
                    title = label
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
            }
        },
        update = { mapView ->
            val point = GeoPoint(latitude, longitude)
            mapView.controller.animateTo(point, 14.0, 800L)

            // Update marker position
            val existingMarker = mapView.overlays
                .filterIsInstance<Marker>()
                .firstOrNull()
            if (existingMarker != null) {
                existingMarker.position = point
                existingMarker.title = label
            } else {
                val marker = Marker(mapView).apply {
                    position = point
                    title = label
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}
