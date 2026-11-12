package com.simats.stocksense.feature.dashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.theme.Amber100
import com.simats.stocksense.core.theme.Amber900
import com.simats.stocksense.core.theme.DarkAmber50
import com.simats.stocksense.core.theme.DarkAmber700
import com.simats.stocksense.core.theme.DarkSurface
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.theme.ThemeMode
import com.simats.stocksense.core.theme.ThemeState
import com.simats.stocksense.feature.dashboard.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()
    val currentThemeMode by ThemeState.themeMode.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadSettings(isRefresh = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle session expiration
    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    val account = uiState.settingsData?.account
    val notifications = uiState.settingsData?.notifications
    val inventory = uiState.settingsData?.inventory

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.65f, size.height * 0.15f)
                            lineTo(size.width * 0.25f, size.height * 0.5f)
                            lineTo(size.width * 0.65f, size.height * 0.85f)
                        }
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${account?.shopName?.takeIf { it.isNotBlank() } ?: "Store"} · Preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Handling: Initial Loading State
            if (uiState.isLoading && uiState.settingsData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (uiState.errorMessage != null && uiState.settingsData == null) {
                // State Handling: Error state with Retry button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Unable to load settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.errorMessage ?: "Settings could not be loaded. Please check your connection and retry.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadSettings() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Content State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Inline Error Banner if a save operation failed
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "Failed to update setting.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                    Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }

                    // 2. App Theme & Appearance Section
                    Text(
                        text = "App Theme & Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select display theme mode:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val modes = listOf(
                                    ThemeMode.LIGHT to "Light",
                                    ThemeMode.DARK to "Dark",
                                    ThemeMode.SYSTEM to "System"
                                )

                                modes.forEach { (mode, label) ->
                                    val isSelected = currentThemeMode == mode
                                    Surface(
                                        onClick = { viewModel.updateThemeMode(mode) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 0.dp else 1.dp,
                                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Account & Store Section
                    Text(
                        text = "Account & store",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Owner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    account?.ownerName?.takeIf { it.isNotBlank() } ?: "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                account?.email?.takeIf { it.isNotBlank() } ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Store", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                val shop = account?.shopName?.takeIf { it.isNotBlank() } ?: "—"
                                val loc = account?.location?.takeIf { it.isNotBlank() }
                                val storeStr = if (loc != null && shop != "—") "$shop, $loc" else shop
                                Text(
                                    storeStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Currency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                val curr = account?.currency?.takeIf { it.isNotBlank() } ?: "INR"
                                val currStr = if (curr.equals("INR", ignoreCase = true)) "Indian rupee · ₹" else curr
                                Text(
                                    currStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Timezone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                val tz = account?.timezone?.takeIf { it.isNotBlank() } ?: "Asia/Kolkata"
                                val tzStr = if (tz.equals("Asia/Kolkata", ignoreCase = true)) "Asia/Kolkata · IST" else tz
                                Text(
                                    tzStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 4. Notification Preferences Section
                    Text(
                        text = "Notification preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Toggle 1: Low-stock alerts
                            TogglePreferenceRow(
                                title = "Low-stock alerts",
                                subtitle = "When stock falls below its minimum",
                                checked = notifications?.lowStockAlerts ?: true,
                                onCheckedChange = { viewModel.updateNotificationSetting("low_stock_alerts", it) },
                                primaryColor = primaryColor
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                            // Toggle 2: AI recommendations
                            TogglePreferenceRow(
                                title = "AI recommendations",
                                subtitle = "Reorders, clearance & smart bundles",
                                checked = notifications?.aiRecommendations ?: true,
                                onCheckedChange = { viewModel.updateNotificationSetting("ai_recommendations", it) },
                                primaryColor = primaryColor
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                            // Toggle 3: Daily sales summary
                            TogglePreferenceRow(
                                title = "Daily sales summary",
                                subtitle = "Your sales snapshot at 9:00 pm IST",
                                checked = notifications?.dailySalesSummary ?: true,
                                onCheckedChange = { viewModel.updateNotificationSetting("daily_sales_summary", it) },
                                primaryColor = primaryColor
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                            // Toggle 4: Weekly email digest (Disabled: backend has no email sender yet)
                            TogglePreferenceRow(
                                title = "Weekly email digest",
                                subtitle = "Weekly summary by email. Available in a future update.",
                                checked = false,
                                onCheckedChange = null,
                                primaryColor = primaryColor,
                                enabled = false,
                                badgeText = "Coming soon",
                                accessibilityDescription = "Weekly email digest, coming soon, unavailable"
                            )
                        }
                    }

                    // 5. Inventory Preferences Section
                    Text(
                        text = "Inventory preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lead time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    "${inventory?.leadTimeDays ?: 2} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Safety buffer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    "${inventory?.safetyDays ?: 3} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cover days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    "${inventory?.coverDays ?: 7} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Slow-moving window", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(
                                    "${inventory?.slowMovingThresholdDays ?: 60} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Import mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                val mode = inventory?.importMode ?: "ADD"
                                val modeStr = if (mode.equals("REPLACE", ignoreCase = true)) "Replace inventory" else "Add received quantities"
                                Text(
                                    modeStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TogglePreferenceRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    primaryColor: Color,
    enabled: Boolean = true,
    badgeText: String? = null,
    accessibilityDescription: String? = null
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(
            if (accessibilityDescription != null) {
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = accessibilityDescription
                }
            } else {
                Modifier
            }
        )

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                if (badgeText != null) {
                    val isDark = MaterialTheme.colorScheme.surface == DarkSurface
                    val badgeBg = if (isDark) DarkAmber50 else Amber100
                    val badgeTextCol = if (isDark) DarkAmber700 else Amber900

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextCol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                disabledUncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                disabledUncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    StockSenseTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        SettingsScreen()
    }
}
