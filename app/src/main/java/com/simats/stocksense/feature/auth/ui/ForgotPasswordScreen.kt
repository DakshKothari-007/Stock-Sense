package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.R
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import kotlinx.coroutines.launch

// Placeholder function for backend integration
fun resetPassword(
    email: String,
    newPassword: String,
    onDone: () -> Unit,
) {
    if (email.isNotEmpty() && newPassword.isNotEmpty()) {
        onDone()
    } else {
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackToLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val primaryColor = MainYellow

    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordLengthValid = newPassword.length >= 8
    val passwordsMatch = newPassword == confirmPassword
    val showMismatchError = confirmPassword.isNotEmpty() && !passwordsMatch

    val isResetEnabled = isEmailValid && isPasswordLengthValid && passwordsMatch

    val windowInfo = rememberWindowSizeInfo()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CreamBackground
    ) { innerPadding ->
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            maxWidth = 480.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Top Header with Shared BoldTopBar
                    BoldTopBar(
                        title = "Reset password",
                        subtitle = "Account recovery",
                        onBackClick = onBackToLoginClick
                    )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Logo Icon (adapts with cream badge in dark mode)
                StockSenseLogo(size = 64.dp)

                Spacer(modifier = Modifier.height(18.dp))

                // Title & Subtitle
                Text(
                    text = "Forgot your password?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your account email and set a new password below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Field 1: Email
                Text(
                    text = "Email address",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "e.g. store@stocksense.in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = InkDark,
                        unfocusedBorderColor = InkDark,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.2f)
                                lineTo(size.width, size.height * 0.2f)
                                lineTo(size.width, size.height * 0.8f)
                                lineTo(0f, size.height * 0.8f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color.Gray,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            val linePath = Path().apply {
                                moveTo(0f, size.height * 0.2f)
                                lineTo(size.width * 0.5f, size.height * 0.55f)
                                lineTo(size.width, size.height * 0.2f)
                            }
                            drawPath(
                                path = linePath,
                                color = Color.Gray,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field 2: New Password
                Text(
                    text = "New password",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Minimum 8 characters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = InkDark,
                        unfocusedBorderColor = InkDark,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val w = size.width
                                val h = size.height
                                val eyePath = Path().apply {
                                    moveTo(0f, h * 0.5f)
                                    quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
                                    quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
                                }
                                drawPath(
                                    path = eyePath,
                                    color = Color.Gray,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.Gray,
                                    radius = w * 0.18f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                                if (!newPasswordVisible) {
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(w * 0.15f, h * 0.15f),
                                        end = Offset(w * 0.85f, h * 0.85f),
                                        strokeWidth = 1.8.dp.toPx()
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field 3: Confirm New Password
                Text(
                    text = "Confirm new password",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Re-enter new password",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    isError = showMismatchError,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = InkDark,
                        unfocusedBorderColor = InkDark,
                        errorBorderColor = StatusDangerRed,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val w = size.width
                                val h = size.height
                                val eyePath = Path().apply {
                                    moveTo(0f, h * 0.5f)
                                    quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
                                    quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
                                }
                                drawPath(
                                    path = eyePath,
                                    color = Color.Gray,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.Gray,
                                    radius = w * 0.18f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                                if (!confirmPasswordVisible) {
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(w * 0.15f, h * 0.15f),
                                        end = Offset(w * 0.85f, h * 0.85f),
                                        strokeWidth = 1.8.dp.toPx()
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true
                )

                if (showMismatchError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoldButton(
                    onClick = {
                        resetPassword(email, newPassword) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Password updated")
                                onBackToLoginClick()
                            }
                        }
                    },
                    enabled = isResetEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                ) {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBackToLoginClick) {
                    Text(
                        text = "Remember password? Back to Log In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Smarter Stock. Better Business.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    StockSenseTheme {
        ForgotPasswordScreen()
    }
}
