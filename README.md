# 🐰 BunnyVPN - Premium Android VPN Demo App

A fully animated, production-grade VPN demo app built with Kotlin + Jetpack Compose.

---

## 📁 Project Structure

```
BunnyVPN/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/bunnyvpn/
│           ├── MainActivity.kt              ← NavHost + Bottom Bar
│           ├── model/Models.kt             ← Data classes + Dummy servers
│           ├── repository/
│           │   ├── PrefsRepository.kt      ← DataStore (auth + settings)
│           │   └── IpRepository.kt         ← Retrofit (ipify.org)
│           ├── viewmodel/MainViewModel.kt  ← All state management
│           ├── navigation/Navigation.kt    ← Routes + BottomNav items
│           └── ui/
│               ├── theme/
│               │   ├── Color.kt
│               │   └── Theme.kt            ← Dark / Light / Neon themes
│               ├── components/Components.kt ← GlassCard, NeonButton, etc.
│               └── screens/
│                   ├── SplashScreen.kt
│                   ├── AuthScreens.kt      ← Login + Signup
│                   ├── HomeScreen.kt       ← Connect button + Map + IP + Speed
│                   ├── ServersScreen.kt
│                   ├── StatsScreen.kt
│                   ├── ProfileScreen.kt
│                   └── SettingsScreen.kt
```

---

## 🚀 Setup Instructions

### Step 1 — Open in Android Studio
- Open Android Studio → **Open** → select the `BunnyVPN` folder
- Wait for Gradle sync to complete

### Step 2 — Add Google Maps API Key
In `AndroidManifest.xml`, replace:
```xml
android:value="PUT_YOUR_GOOGLE_MAPS_API_KEY_HERE"
```
with your actual key from [Google Cloud Console](https://console.cloud.google.com).

**Enable these APIs in your Google Cloud project:**
- Maps SDK for Android
- Geocoding API

### Step 3 — Run on Device / Emulator
- Minimum SDK: 26 (Android 8.0)
- Real device recommended for SIM info and GPS

---

## ✅ Features

| Feature | Status |
|---|---|
| Dark / Light / Neon themes | ✅ |
| Animated splash screen | ✅ |
| Login / Signup (DataStore) | ✅ |
| Auto-login | ✅ |
| Connect button (breathe / rotate / ripple) | ✅ |
| Animated gradient background | ✅ |
| Google Maps (dark theme) | ✅ |
| Real GPS location + Geocoder | ✅ |
| Public IP (ipify.org) | ✅ |
| Real-time network speed (TrafficStats) | ✅ |
| SIM operator + network type | ✅ |
| Server list with ping badges | ✅ |
| Stats screen with speed bars | ✅ |
| Profile edit | ✅ |
| Logout | ✅ |
| Settings (theme, auto-connect) | ✅ |
| Bottom nav with glow animation | ✅ |
| Screen transitions (fade+slide) | ✅ |
| Animated counters | ✅ |

---

## 🔑 Permissions Used

```xml
INTERNET
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
READ_PHONE_STATE
```

---

## 🛠 Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Navigation Compose** — multi-screen
- **MVVM** — ViewModel + StateFlow
- **DataStore Preferences** — persistent auth & settings
- **Retrofit** — IP fetch (ipify.org)
- **Google Maps Compose** — dark map
- **Coroutines** — async data
- **TrafficStats** — real network speed
- **TelephonyManager** — SIM info

---

## 👨‍💻 Developer
Built by **Bunny** — Portfolio Project
