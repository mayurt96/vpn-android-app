package com.bunnyvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.bunnyvpn.ui.components.AnimatedGradientBackground
import com.bunnyvpn.ui.theme.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

@Composable
fun ChatScreen() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = input.trim()
        if (text.isEmpty() || isLoading) return
        input = ""
        messages.add(ChatMessage("user", text))
        isLoading = true

        scope.launch(Dispatchers.IO) {
            try {
                val apiKey = "3edd68ac1d75457796a7a6cc70a951f8.6mKFeVnl4tJ2RuG5"

                val messagesArray = JSONArray()
                messages.filter { !it.content.startsWith("API Error:") && !it.content.startsWith("Network Error:") }
                    .forEach { msg ->
                        val msgObj = JSONObject()
                        msgObj.put("role", if (msg.role == "user") "user" else "assistant")
                        msgObj.put("content", msg.content)
                        messagesArray.put(msgObj)
                    }

                if (messagesArray.length() == 0) {
                    withContext(Dispatchers.Main) { isLoading = false }
                    return@launch
                }

                val body = JSONObject().apply {
                    put("model", "glm-4")
                    put("messages", messagesArray)
                }.toString()

                val url = URL("https://open.bigmodel.cn/api/paas/v4/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    doOutput = true
                    connectTimeout = 30000
                    readTimeout = 30000
                }

                OutputStreamWriter(conn.outputStream).use { it.write(body) }

                val responseCode = conn.responseCode
                val response = if (responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Error code: $responseCode"
                }

                if (responseCode == 200) {
                    val json = JSONObject(response)
                    val reply = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    withContext(Dispatchers.Main) {
                        messages.add(ChatMessage("assistant", reply))
                        isLoading = false
                    }
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(response)
                        errJson.optJSONObject("error")?.optString("message") ?: response
                    } catch (e: Exception) {
                        response
                    }
                    withContext(Dispatchers.Main) {
                        messages.add(ChatMessage("assistant", "API Error: $errorMsg"))
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messages.add(ChatMessage("assistant", "Network Error: ${e.localizedMessage ?: "Unknown error"}"))
                    isLoading = false
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.radialGradient(listOf(Cyan400.copy(0.3f), Purple400.copy(0.2f))),
                                CircleShape
                            )
                            .border(1.dp, Brush.sweepGradient(listOf(Cyan400, Purple400)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = Cyan400,
                            modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("BunnyAI", fontWeight = FontWeight.Bold,
                            color = TextPrimary, fontSize = 16.sp)
                        Text("Online Chat Assistant", color = TextSecondary, fontSize = 11.sp)
                    }

                    Spacer(Modifier.weight(1f))

                    // Online indicator
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(7.dp).background(StatusConnected, CircleShape))
                        Text("Online", color = StatusConnected, fontSize = 11.sp)
                    }
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        // Welcome card
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        Brush.radialGradient(listOf(Cyan400.copy(0.2f), Purple400.copy(0.1f))),
                                        CircleShape
                                    )
                                    .border(2.dp, Brush.sweepGradient(listOf(Cyan400, Purple400, Cyan400)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AutoAwesome, null, tint = Cyan400,
                                    modifier = Modifier.size(36.dp))
                            }
                            Text("BunnyAI", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                                )
                            )
                            Text("Powered by Gemini AI", color = TextSecondary, fontSize = 13.sp)

                            Spacer(Modifier.height(8.dp))

                            listOf("🔒 VPN ke baare mein pooch", "🌍 Network security tips", "💡 Kuch bhi pooch!").forEach { hint ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(GlassWhite)
                                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                        .clickable { 
                                            // Extract text safely after emoji/space
                                            val hintText = hint.split(" ", limit = 2).lastOrNull() ?: hint
                                            input = hintText
                                            sendMessage()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(hint, color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubble(msg)
                }

                if (isLoading) {
                    item { TypingIndicator() }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message BunnyAI...", color = TextMuted, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Cyan400,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )

                // Send button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (input.isNotEmpty() && !isLoading)
                                Brush.linearGradient(listOf(Cyan400, Purple400))
                            else
                                Brush.linearGradient(listOf(GlassWhite, GlassWhite)),
                            CircleShape
                        )
                        .clickable(
                            enabled = input.isNotEmpty() && !isLoading,
                            onClickLabel = "Send message",
                            onClick = { sendMessage() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (input.isNotEmpty() && !isLoading) Color.White else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.radialGradient(listOf(Cyan400.copy(0.3f), Purple400.copy(0.2f))),
                        CircleShape
                    )
                    .border(1.dp, Cyan400.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Cyan400, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(Cyan400.copy(0.8f), Purple400.copy(0.8f)))
                    else
                        Brush.linearGradient(listOf(GlassWhite, GlassWhite))
                )
                .border(
                    1.dp,
                    if (isUser) Color.Transparent else GlassBorder,
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isUser) Color.White else TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dot"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Brush.radialGradient(listOf(Cyan400.copy(0.3f), Purple400.copy(0.2f))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Cyan400, modifier = Modifier.size(16.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(GlassWhite)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                Cyan400.copy(alpha = alpha * (if (i == 0) 1f else if (i == 1) 0.7f else 0.4f)),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}
