package com.bunnyvpn.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.Build
import android.telephony.TelephonyManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunnyvpn.model.*
import com.bunnyvpn.repository.IpRepository
import com.bunnyvpn.repository.PrefsRepository
import com.bunnyvpn.repository.VpnRepository
import com.bunnyvpn.service.BunnyVpnService
import com.bunnyvpn.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsRepository(application)
    private val vpnRepo = VpnRepository(application)
    private val appContext = application.applicationContext

    // ---------- User / Auth ----------
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ---------- App Theme ----------
    private val _appTheme = MutableStateFlow(AppTheme.DARK)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private val _autoConnect = MutableStateFlow(false)
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()

    // ---------- VPN State ----------
    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _selectedServer = MutableStateFlow(DummyServers[0])
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _connectionTimer = MutableStateFlow(0L)
    val connectionTimer: StateFlow<Long> = _connectionTimer.asStateFlow()

    // ---------- Device Info ----------
    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    // ---------- Network Stats ----------
    private val _networkStats = MutableStateFlow(NetworkStats())
    val networkStats: StateFlow<NetworkStats> = _networkStats.asStateFlow()

    // ---------- Error / Toast ----------
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private var timerJob: Job? = null
    private var statsJob: Job? = null

    init {
        viewModelScope.launch {
            prefs.userProfileFlow.collect { profile ->
                _userProfile.value = profile
                _isLoggedIn.value = profile.isLoggedIn
            }
        }
        viewModelScope.launch { prefs.appThemeFlow.collect { _appTheme.value = it } }
        viewModelScope.launch { prefs.autoConnectFlow.collect { _autoConnect.value = it } }

        fetchPublicIp()
        startNetworkStats()
        loadSimInfo(appContext)
    }

    // ---- Auth ----
    fun login(email: String, password: String): Boolean {
        val profile = _userProfile.value
        return if (profile.email == email && profile.password == password) {
            viewModelScope.launch { prefs.saveUser(profile.copy(isLoggedIn = true)) }
            true
        } else false
    }

    fun signup(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch { prefs.saveUser(UserProfile(name, email, phone, password, true)) }
    }

    fun logout() { viewModelScope.launch { prefs.logout() } }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch { prefs.saveUser(_userProfile.value.copy(name = name, email = email, phone = phone)) }
    }

    // ---- VPN ----
    fun toggleVpn() {
        when (_vpnState.value) {
            VpnState.DISCONNECTED -> connect()
            VpnState.CONNECTED -> disconnect()
            VpnState.CONNECTING -> {}
        }
    }

    private fun connect() {
        viewModelScope.launch {
            _vpnState.value = VpnState.CONNECTING
            delay(2500)
            _vpnState.value = VpnState.CONNECTED
            _deviceInfo.value = _deviceInfo.value.copy(fakeIp = _selectedServer.value.fakeIp)
            startTimer()
        }
    }

    private fun disconnect() {
        _vpnState.value = VpnState.DISCONNECTED
        timerJob?.cancel()
        _connectionTimer.value = 0L
        fetchPublicIp()
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        if (_vpnState.value == VpnState.CONNECTED) disconnect()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) { delay(1000); _connectionTimer.value++ }
        }
    }

    // ---- IP ----
    private fun fetchPublicIp() {
        viewModelScope.launch {
            val ip = IpRepository.fetchPublicIp()
            _deviceInfo.value = _deviceInfo.value.copy(realIp = ip)
        }
    }

    // ---- Location ----
    fun updateLocation(context: Context, lat: Double, lng: Double) {
        // Immediately update coordinates so map moves
        _deviceInfo.value = _deviceInfo.value.copy(
            latitude = lat,
            longitude = lng,
            city = "Locating...",
            fullAddress = "Fetching address..."
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val city = address?.locality
                            ?: address?.subAdminArea
                            ?: address?.adminArea
                            ?: "Unknown"
                        val fullAddr = address?.getAddressLine(0) ?: "Address unavailable"
                        viewModelScope.launch(Dispatchers.Main) {
                            _deviceInfo.value = _deviceInfo.value.copy(
                                city = city,
                                fullAddress = fullAddr,
                                latitude = lat,
                                longitude = lng
                            )
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()
                    val city = address?.locality
                        ?: address?.subAdminArea
                        ?: address?.adminArea
                        ?: "Unknown"
                    val fullAddr = address?.getAddressLine(0) ?: "Address unavailable"
                    withContext(Dispatchers.Main) {
                        _deviceInfo.value = _deviceInfo.value.copy(
                            city = city,
                            fullAddress = fullAddr,
                            latitude = lat,
                            longitude = lng
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _deviceInfo.value = _deviceInfo.value.copy(
                        city = "Unknown",
                        fullAddress = "Location: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    )
                }
            }
        }
    }

    // ---- SIM Info — properly detects WiFi vs Mobile ----
    fun loadSimInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(network)

                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                val operator = if (isCellular) {
                    tm.networkOperatorName.ifBlank { tm.simOperatorName.ifBlank { "Unknown" } }
                } else {
                    // On WiFi, still read SIM card name
                    tm.simOperatorName.ifBlank { "SIM Unavailable" }
                }

                val networkType = when {
                    isWifi -> {
                        val wifiMgr = context.applicationContext
                            .getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                        @Suppress("DEPRECATION")
                        val info = wifiMgr.connectionInfo
                        val ssid = info?.ssid?.replace("\"", "")?.ifBlank { null }
                        if (ssid != null && ssid != "<unknown ssid>") "WiFi ($ssid)" else "WiFi"
                    }
                    isCellular -> {
                        when (tm.dataNetworkType) {
                            TelephonyManager.NETWORK_TYPE_NR -> "5G"
                            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                            TelephonyManager.NETWORK_TYPE_HSPAP,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA -> "3G HSPA"
                            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                            TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
                            TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
                            else -> "Mobile Data"
                        }
                    }
                    else -> "No Network"
                }

                withContext(Dispatchers.Main) {
                    _deviceInfo.value = _deviceInfo.value.copy(
                        simOperator = operator,
                        networkType = networkType
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _deviceInfo.value = _deviceInfo.value.copy(
                        simOperator = "Unknown",
                        networkType = "Unknown"
                    )
                }
            }
        }
    }

    // ---- Network Speed — real per-second delta ----
    private fun startNetworkStats() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch(Dispatchers.IO) {
            var prevRx = TrafficStats.getUidRxBytes(android.os.Process.myUid())
            var prevTx = TrafficStats.getUidTxBytes(android.os.Process.myUid())
            var prevTime = System.currentTimeMillis()

            while (true) {
                delay(1000)
                val currRx = TrafficStats.getUidRxBytes(android.os.Process.myUid())
                val currTx = TrafficStats.getUidTxBytes(android.os.Process.myUid())
                val currTime = System.currentTimeMillis()

                val elapsed = (currTime - prevTime).coerceAtLeast(1L)
                val dlKbps = ((currRx - prevRx).toFloat() * 1000f / elapsed / 1024f).coerceAtLeast(0f)
                val ulKbps = ((currTx - prevTx).toFloat() * 1000f / elapsed / 1024f).coerceAtLeast(0f)

                prevRx = currRx
                prevTx = currTx
                prevTime = currTime

                withContext(Dispatchers.Main) {
                    _networkStats.value = NetworkStats(
                        downloadSpeed = dlKbps,
                        uploadSpeed = ulKbps,
                        downloadTotal = currRx.coerceAtLeast(0L),
                        uploadTotal = currTx.coerceAtLeast(0L)
                    )
                }
            }
        }
    }

    // ---- Settings ----
    fun setTheme(theme: AppTheme) { viewModelScope.launch { prefs.saveTheme(theme) } }
    fun setAutoConnect(enabled: Boolean) { viewModelScope.launch { prefs.saveAutoConnect(enabled) } }
    fun clearError() { _errorMsg.value = null }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        statsJob?.cancel()
    }
}
