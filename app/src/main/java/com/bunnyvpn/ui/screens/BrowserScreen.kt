package com.bunnyvpn.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.bunnyvpn.ui.components.AnimatedGradientBackground
import com.bunnyvpn.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen() {
    var url by remember { mutableStateOf("https://www.google.com") }
    var urlInput by remember { mutableStateOf(url) }
    var isLoading by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("Google") }
    var progress by remember { mutableStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    // Handle back press
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    fun navigateToUrl(input: String) {
        val finalUrl = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }
        url = finalUrl
        webViewRef?.loadUrl(finalUrl)
        keyboard?.hide()
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(Modifier.fillMaxSize()) {

            // === URL BAR ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back button
                IconButton(
                    onClick = { webViewRef?.goBack() },
                    enabled = webViewRef?.canGoBack() == true
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = if (webViewRef?.canGoBack() == true) Cyan400 else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // URL input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = {
                        Text("Search or enter URL...", color = TextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            if (isLoading) Icons.Filled.Refresh else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (isLoading) StatusConnecting else StatusConnected,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (urlInput.isNotEmpty()) {
                            IconButton(onClick = { urlInput = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = null,
                                    tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { if (urlInput.isNotEmpty()) navigateToUrl(urlInput) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Cyan400,
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                // Refresh
                IconButton(onClick = { webViewRef?.reload() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null,
                        tint = Cyan400, modifier = Modifier.size(20.dp))
                }
            }

            // Progress bar
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Cyan400,
                    trackColor = GlassWhite
                )
            }

            // === WEBVIEW ===
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isLoading = true
                                url?.let { urlInput = it }
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                pageTitle = view?.title ?: ""
                                url?.let { urlInput = it }
                            }
                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                                handler?.proceed() // Proceed for debugging/common issues
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                android.util.Log.e("BrowserScreen", "Error: ${error?.description}")
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                pageTitle = title ?: ""
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
