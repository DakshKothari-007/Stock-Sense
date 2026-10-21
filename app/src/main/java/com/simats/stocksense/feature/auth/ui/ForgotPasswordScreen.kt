package com.simats.stocksense.feature.auth.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun ForgotPasswordScreen(
    onBackToLoginClick: () -> Unit = {},
    onSendResetLinkClick: (String) -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.forgotPasswordState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

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
                // 1. Top Header with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToLoginClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.7f, size.height * 0.2f)
                                lineTo(size.width * 0.3f, size.height * 0.5f)
                                lineTo(size.width * 0.7f, size.height * 0.8f)
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Reset password",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "ACCOUNT RECOVERY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 2. Key Icon Box
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
                    text = "Forgot your password?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the email associated with your StockSense store and we'll send a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

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
                    value = state.email,
                    onValueChange = { viewModel.updateForgotPasswordEmail(it) },
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

                // Success Message Banner if submitted
                if (state.isSubmitted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.1f, size.height * 0.55f)
                                    lineTo(size.width * 0.35f, size.height * 0.8f)
                                    lineTo(size.width * 0.75f, size.height * 0.25f)
                                }
                                drawPath(
                                    path = path,
                                    color = primaryColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Reset link has been sent to ${state.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset()
                        onSendResetLinkClick(state.email)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (state.isSubmitted) "Resend Link" else "Send Reset Link",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
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

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    StockSenseTheme {
        ForgotPasswordScreen()
    }
}
