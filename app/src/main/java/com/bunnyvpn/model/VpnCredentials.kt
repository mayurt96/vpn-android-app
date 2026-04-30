package com.bunnyvpn.model

// VPNBook credentials - update from vpnbook.com (they change monthly)
object VpnBookCredentials {
    const val USERNAME = "vpnbook"
    const val PASSWORD = "abc123" // Check vpnbook.com for current password
    
    // .ovpn file names from assets/vpn/ folder
    val SERVER_CONFIGS = listOf(
        VpnConfig("🇺🇸 USA #16", "vpn/vpnbook-us16-tcp443.ovpn"),
        VpnConfig("🇬🇧 UK #1",  "vpn/uk1.ovpn"),
        VpnConfig("🇩🇪 EU #1",  "vpn/euro1.ovpn"),
        VpnConfig("🇨🇦 Canada", "vpn/ca1.ovpn")
    )
}

data class VpnConfig(
    val displayName: String,
    val assetPath: String
)
