package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.R
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // 1. Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "YOUR SHOP, IN FOCUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Logo Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "StockSense Logo",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Subtitle
                Text(
                    text = "A smarter day starts here.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Log in to your StockSense workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                val primaryColor = MaterialTheme.colorScheme.primary

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRect(
                                color = Color.Gray,
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
                                color = Color.Gray,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    },
                    singleLine = true
                )

                // Forgot Password link right under password field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Green Basket Demo Credentials Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.52f)
                                lineTo(size.width * 0.42f, size.height * 0.74f)
                                lineTo(size.width * 0.8f, size.height * 0.26f)
                            }
                            drawCircle(
                                color = primaryColor,
                                radius = size.width / 2f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Explore the Green Basket demo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sample credentials are filled in. No real account or data is used.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
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
                // Log In Button
                Button(
                    onClick = {
                        viewModel.login(onSuccess = onLoginClick)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continue with Google Button
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onSignUpClick) {
                    Text(
                        text = "New to StockSense? Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StockSenseTheme {
        LoginScreen()
    }
}
