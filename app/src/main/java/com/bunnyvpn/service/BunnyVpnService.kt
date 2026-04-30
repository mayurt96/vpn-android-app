package com.bunnyvpn.service

import android.net.VpnService

class BunnyVpnService : VpnService() {
    companion object {
        const val ACTION_CONNECT = "com.bunnyvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.bunnyvpn.DISCONNECT"
        const val EXTRA_CONFIG = "vpn_config"
        const val EXTRA_USERNAME = "vpn_username"
        const val EXTRA_PASSWORD = "vpn_password"
        
        var isRunning = false
    }
}
