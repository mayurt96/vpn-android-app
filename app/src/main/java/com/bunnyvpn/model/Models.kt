package com.bunnyvpn.model

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false
)

data class VpnServer(
    val id: String,
    val country: String,
    val city: String,
    val flag: String,
    val ping: Int,
    val load: Int,
    val fakeIp: String,
    val vpnConfig: VpnConfig? = null
)

data class NetworkStats(
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val downloadTotal: Long = 0L,
    val uploadTotal: Long = 0L
)

data class DeviceInfo(
    val realIp: String = "Fetching...",
    val fakeIp: String = "10.8.0.2",
    val simOperator: String = "Unknown",
    val networkType: String = "Unknown",
    val city: String = "Unknown",
    val fullAddress: String = "Fetching location...",
    val latitude: Double = 20.9042,
    val longitude: Double = 74.7749
)

enum class VpnState {
    DISCONNECTED, CONNECTING, CONNECTED
}

data class IpResponse(val ip: String)

val DummyServers = listOf(
    VpnServer("in1", "India", "Mumbai", "🇮🇳", 12, 45, "103.21.58.100"),
    VpnServer("us1", "United States", "New York", "🇺🇸", 180, 62, "147.135.15.16", VpnConfig("🇺🇸 USA #16", "vpn/vpnbook-us16-tcp443.ovpn")),
    VpnServer("de1", "Germany", "Frankfurt", "🇩🇪", 140, 38, "78.47.204.33"),
    VpnServer("sg1", "Singapore", "Singapore", "🇸🇬", 95, 55, "139.59.1.24"),
    VpnServer("uk1", "United Kingdom", "London", "🇬🇧", 160, 48, "185.220.101.10"),
    VpnServer("jp1", "Japan", "Tokyo", "🇯🇵", 210, 30, "45.76.131.120")
)
