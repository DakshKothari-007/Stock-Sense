package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.SecureScreenInReleaseEffect
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel
import com.simats.stocksense.feature.auth.viewmodel.DeleteAccountUiState

@Composable
fun DeleteAccountScreen(
    shopName: String = "",
    onBackClick: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    onSessionExpired: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    SecureScreenInReleaseEffect()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.deleteAccountState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearDeleteAccountState()
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    DeleteAccountScreenContent(
        shopName = shopName,
        uiState = uiState,
        onBackClick = onBackClick,
        onPasswordChange = { viewModel.updateDeletePassword(it) },
        onConfirmTextChange = { viewModel.updateDeleteConfirmText(it) },
        onExportCsvClick = { chosenName -> viewModel.exportProductsCsv(context, chosenName) },
        onDeleteClick = {
            viewModel.deleteAccount(
                onSuccess = { onAccountDeleted() },
                onSessionExpired = onSessionExpired
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreenContent(
    shopName: String,
    uiState: DeleteAccountUiState,
    onBackClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmTextChange: (String) -> Unit,
    onExportCsvClick: (String) -> Unit = {},
    onDeleteClick: () -> Unit
) {
    val primaryColor = MainYellow
    val iconStrokeColor = InkDark
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    val isDeleteButtonEnabled = uiState.password.isNotBlank() &&
            uiState.confirmText == "DELETE" &&
            !uiState.isLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        val windowInfo = rememberWindowSizeInfo()

        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            maxWidth = 520.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                val formattedShopName = shopName.trim().ifBlank { "Store" }

                // 1. Header with Shared BoldTopBar
                BoldTopBar(
                    title = "Delete account",
                    subtitle = "$formattedShopName · owner account",
                    onBackClick = onBackClick
                )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Warning Card ("THIS CANNOT BE UNDONE")
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SoftPink,
                borderColor = InkDark,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "THIS CANNOT BE UNDONE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusDangerRed,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Your shop and all its data will be removed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Products, sales, orders, insights and settings are deleted. You are signed out on every device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Password Input Field
            Text(
                text = "Your password",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Enter your current password",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
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
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Confirmation Text Field ("DELETE")
            Text(
                text = "Type DELETE to confirm",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.confirmText,
                onValueChange = onConfirmTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "DELETE",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = if (uiState.confirmText == "DELETE") StatusDangerRed else primaryColor,
                    unfocusedBorderColor = if (uiState.confirmText == "DELETE") StatusDangerRed.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Informational Tip & CSV Export Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawCircle(
                                color = MainBlue,
                                radius = size.width / 2f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawLine(
                                color = MainBlue,
                                start = Offset(size.width * 0.5f, size.height * 0.42f),
                                end = Offset(size.width * 0.5f, size.height * 0.75f),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = MainBlue,
                                radius = 1.2.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.28f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Want a copy first? Export your product list as a CSV file before you delete.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    ExportProductsCsvAction(
                        isExporting = uiState.isExportingCsv,
                        exportErrorMessage = uiState.exportErrorMessage,
                        enabled = !uiState.isLoading,
                        onConfirmExport = onExportCsvClick
                    )
                }
            }

            // 6. Error Banner (Shows real backend error message)
            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 7. Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delete My Account Button (Disabled until password is non-empty & confirmText == "DELETE")
                BoldButton(
                    onClick = onDeleteClick,
                    enabled = isDeleteButtonEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = StatusDangerRed,
                    contentColor = Color.White
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Delete my account",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }

                // Keep My Account Button
                BoldButton(
                    onClick = onBackClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                ) {
                    Text(
                        text = "Keep my account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Preview(showBackground = true)
@Composable
fun DeleteAccountScreenPreview() {
    StockSenseTheme {
        DeleteAccountScreenContent(
            shopName = "Store",
            uiState = DeleteAccountUiState(
                password = "",
                confirmText = "DELETE",
                isLoading = false
            ),
            onBackClick = {},
            onPasswordChange = {},
            onConfirmTextChange = {},
            onDeleteClick = {}
        )
    }
}
