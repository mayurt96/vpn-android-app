package com.bunnyvpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.bunnyvpn.ui.components.AnimatedGradientBackground
import com.bunnyvpn.ui.theme.*
import com.bunnyvpn.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = Cyan400,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "BunnyVPN",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )
            Text("Sign in to continue", color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(40.dp))

            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassWhite)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpnTextField(
                    value = email,
                    onValueChange = { email = it; errorText = "" },
                    label = "Email",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email
                )

                VpnTextField(
                    value = password,
                    onValueChange = { password = it; errorText = "" },
                    label = "Password",
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                if (errorText.isNotEmpty()) {
                    Text(errorText, color = StatusDisconnected, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorText = "Please fill all fields"
                            return@Button
                        }
                        isLoading = true
                        val success = viewModel.login(email.trim(), password)
                        isLoading = false
                        if (success) onLoginSuccess()
                        else errorText = "Invalid email or password"
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Cyan400, Purple400))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("LOGIN", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    "Sign Up",
                    color = Cyan400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateSignup() }
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    viewModel: MainViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Cyan400, Purple400))
                )
            )
            Text("Join BunnyVPN today", color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassWhite)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VpnTextField(name, { name = it }, "Full Name", Icons.Filled.Person)
                VpnTextField(email, { email = it }, "Email", Icons.Filled.Email, keyboardType = KeyboardType.Email)
                VpnTextField(phone, { phone = it }, "Phone", Icons.Filled.Phone, keyboardType = KeyboardType.Phone)
                VpnTextField(
                    password, { password = it }, "Password", Icons.Filled.Lock,
                    isPassword = true, passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                if (errorText.isNotEmpty()) {
                    Text(errorText, color = StatusDisconnected, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        when {
                            name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() ->
                                errorText = "Please fill all fields"
                            !email.contains("@") -> errorText = "Invalid email"
                            password.length < 6 -> errorText = "Password min 6 characters"
                            else -> {
                                viewModel.signup(name.trim(), email.trim(), phone.trim(), password)
                                onSignupSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Purple400, Cyan400))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    "Login",
                    color = Cyan400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateLogin() }
                )
            }
        }
    }
}

@Composable
fun VpnTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Cyan400,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Cyan400,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
