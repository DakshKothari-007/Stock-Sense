package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.R
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val primaryColor = MainYellow
    val iconStrokeColor = InkDark
    var passwordVisible by remember { mutableStateOf(false) }
    val windowInfo = rememberWindowSizeInfo()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
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
                    Spacer(modifier = Modifier.height(if (windowInfo.isLandscape) 4.dp else 12.dp))

                    // 1. Top Header
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "YOUR SHOP, IN FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(if (windowInfo.isLandscape) 12.dp else 24.dp))

                    // 2. Logo Icon (Compact in landscape/short screen, badge in dark mode)
                    StockSenseLogo(
                        size = if (windowInfo.isLandscape || windowInfo.screenHeightDp < 600.dp) 48.dp else 64.dp
                    )

                    Spacer(modifier = Modifier.height(if (windowInfo.isLandscape) 12.dp else 24.dp))

                    // Title & Subtitle
                    Text(
                        text = "A smarter day starts\nhere.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (windowInfo.screenWidthDp < 360.dp) 24.sp else 28.sp,
                        lineHeight = if (windowInfo.screenWidthDp < 360.dp) 30.sp else 34.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Log in to your StockSense workspace.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(if (loginState.lockRetryAfterSeconds > 0 || !loginState.bannerError.isNullOrBlank()) 14.dp else 28.dp))

                    // Account Locked Banner
                    if (!loginState.bannerError.isNullOrBlank() || loginState.lockRetryAfterSeconds > 0) {
                        val bannerText = loginState.bannerError ?: "Too many failed attempts. Try again in ${loginState.lockRetryAfterSeconds}s"
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftPeach,
                            borderColor = DangerRed,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    drawRect(
                                        color = DangerRed,
                                        topLeft = Offset(size.width * 0.15f, size.height * 0.45f),
                                        size = Size(size.width * 0.7f, size.height * 0.5f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    val archPath = Path().apply {
                                        moveTo(size.width * 0.3f, size.height * 0.45f)
                                        lineTo(size.width * 0.3f, size.height * 0.25f)
                                        quadraticTo(size.width * 0.5f, 0f, size.width * 0.7f, size.height * 0.25f)
                                        lineTo(size.width * 0.7f, size.height * 0.45f)
                                    }
                                    drawPath(
                                        path = archPath,
                                        color = DangerRed,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawCircle(
                                        color = DangerRed,
                                        radius = size.width * 0.08f,
                                        center = Offset(size.width * 0.5f, size.height * 0.65f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Account Locked",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = bannerText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = InkDark,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                // 3. Email Input Field
                Text(
                    text = "Email address",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = loginState.email,
                    onValueChange = { viewModel.updateLoginEmail(it) },
                    placeholder = {
                        Text(
                            text = "Enter your email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = if (!loginState.emailError.isNullOrBlank()) DangerRed else InkDark,
                        unfocusedBorderColor = if (!loginState.emailError.isNullOrBlank()) DangerRed else InkDark,
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
                                color = iconStrokeColor,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            val linePath = Path().apply {
                                moveTo(0f, size.height * 0.2f)
                                lineTo(size.width * 0.5f, size.height * 0.55f)
                                lineTo(size.width, size.height * 0.2f)
                            }
                            drawPath(
                                path = linePath,
                                color = iconStrokeColor,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    },
                    singleLine = true
                )
                if (!loginState.emailError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loginState.emailError ?: "",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Password Input Field
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = loginState.password,
                    onValueChange = { viewModel.updateLoginPassword(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = if (!loginState.passwordError.isNullOrBlank()) DangerRed else InkDark,
                        unfocusedBorderColor = if (!loginState.passwordError.isNullOrBlank()) DangerRed else InkDark,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        cursorColor = InkDark
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRect(
                                color = iconStrokeColor,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.45f),
                                size = Size(size.width * 0.7f, size.height * 0.5f),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            val archPath = Path().apply {
                                moveTo(size.width * 0.3f, size.height * 0.45f)
                                lineTo(size.width * 0.3f, size.height * 0.25f)
                                quadraticTo(size.width * 0.5f, 0f, size.width * 0.7f, size.height * 0.25f)
                                lineTo(size.width * 0.7f, size.height * 0.45f)
                            }
                            drawPath(
                                path = archPath,
                                color = iconStrokeColor,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
                                    color = iconStrokeColor,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                drawCircle(
                                    color = iconStrokeColor,
                                    radius = w * 0.18f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                                if (!passwordVisible) {
                                    drawLine(
                                        color = iconStrokeColor,
                                        start = Offset(w * 0.15f, h * 0.15f),
                                        end = Offset(w * 0.85f, h * 0.85f),
                                        strokeWidth = 1.8.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true
                )
                if (!loginState.passwordError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loginState.passwordError ?: "",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Forgot password link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Green Basket Demo Credentials Box
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SoftLightBlue,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.25f, size.height * 0.5f)
                                lineTo(size.width * 0.45f, size.height * 0.7f)
                                lineTo(size.width * 0.75f, size.height * 0.3f)
                            }
                            drawCircle(
                                color = InkDark,
                                radius = size.width / 2f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawPath(
                                path = path,
                                color = InkDark,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Explore the Green Basket demo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sample credentials are filled in. No real account or data is used.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkDark.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Bottom Actions & Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error Message Display
                if (!loginState.errorMessage.isNullOrBlank()) {
                    Text(
                        text = loginState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Log In Button
                BoldButton(
                    onClick = {
                        viewModel.login(onSuccess = onLoginClick)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loginState.isLoading && loginState.lockRetryAfterSeconds == 0,
                    isPrimary = true
                ) {
                    if (loginState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = InkDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Log In",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "New to StockSense? Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Smarter Stock. Better Business.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StockSenseTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenDarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        LoginScreen()
    }
}
