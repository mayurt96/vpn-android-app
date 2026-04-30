package com.bunnyvpn.repository

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.bunnyvpn.model.VpnConfig
import com.bunnyvpn.model.VpnBookCredentials
import com.bunnyvpn.service.BunnyVpnService

class VpnRepository(private val context: Context) {

    // Read .ovpn file from assets
    fun loadOvpnConfig(assetPath: String): String {
        return try {
            context.assets.open(assetPath)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    // Check if VPN permission is needed
    fun prepareVpnIntent(): Intent? {
        return VpnService.prepare(context)
    }

    // Start real VPN
    fun connect(config: VpnConfig) {
        val ovpnContent = loadOvpnConfig(config.assetPath)
        if (ovpnContent.isEmpty()) return

        val intent = Intent(context, BunnyVpnService::class.java).apply {
            action = BunnyVpnService.ACTION_CONNECT
            putExtra(BunnyVpnService.EXTRA_CONFIG, ovpnContent)
            putExtra(BunnyVpnService.EXTRA_USERNAME, VpnBookCredentials.USERNAME)
            putExtra(BunnyVpnService.EXTRA_PASSWORD, VpnBookCredentials.PASSWORD)
        }
        context.startService(intent)
    }

    // Stop VPN
    fun disconnect() {
        val intent = Intent(context, BunnyVpnService::class.java).apply {
            action = BunnyVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun isConnected() = BunnyVpnService.isRunning
}
