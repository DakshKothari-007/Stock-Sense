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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.BuildConfig
import com.simats.stocksense.core.network.ApiConstants
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.dashboard.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onAvatarClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
        color = CreamBackground
    ) {
        val windowInfo = rememberWindowSizeInfo()

        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 640.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Shared BoldTopBar
                BoldTopBar(
                    title = "Settings",
                    subtitle = "${account?.shopName?.takeIf { it.isNotBlank() } ?: "Store"} · Preferences",
                    onBackClick = onBackClick
                )

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
                            color = MainBlue,
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
                                color = InkDark
                            )
                            Text(
                                text = uiState.errorMessage ?: "Settings could not be loaded. Please check your connection and retry.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk,
                                textAlign = TextAlign.Center
                            )
                            BoldButton(
                                text = "Retry",
                                isPrimary = true,
                                onClick = { viewModel.loadSettings() }
                            )
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
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = SoftPink,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uiState.errorMessage ?: "Failed to update setting.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusDangerRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                        Text("Dismiss", color = InkDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2. App Theme & Appearance Section
                        Text(
                            text = "App Theme & Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Select display theme mode:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
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
                                            color = if (isSelected) MainYellow else RaisedSurface,
                                            border = BorderStroke(2.dp, InkDark)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSelected) Color(0xFF0B1230) else InkDark,
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
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAvatarClick() }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        UserAvatarBadge(
                                            avatarCode = uiState.avatarCode,
                                            displayName = account?.ownerName,
                                            size = 52.dp,
                                            borderWidth = 2.dp,
                                            fontSize = 18.sp,
                                            showEditBadge = true,
                                            contentDescription = "Customize avatar",
                                            onClick = onAvatarClick
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = account?.ownerName?.takeIf { it.isNotBlank() } ?: "Owner Avatar",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDark,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (uiState.avatarCode != null) {
                                                    "Tap to change or remove avatar"
                                                } else {
                                                    "Tap to choose a cartoon avatar"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedInk,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Edit →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MainBlue,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                HorizontalDivider(color = InkDark.copy(alpha = 0.12f), thickness = 1.dp)

                                BoldInfoRow(
                                    label = "Owner",
                                    value = account?.ownerName?.takeIf { it.isNotBlank() } ?: "—"
                                )

                                BoldInfoRow(
                                    label = "Email",
                                    value = account?.email?.takeIf { it.isNotBlank() } ?: "—"
                                )

                                HorizontalDivider(color = InkDark.copy(alpha = 0.12f), thickness = 1.dp)

                                val shop = account?.shopName?.takeIf { it.isNotBlank() } ?: "—"
                                val loc = account?.location?.takeIf { it.isNotBlank() }
                                val storeStr = if (loc != null && shop != "—") "$shop, $loc" else shop
                                BoldInfoRow(
                                    label = "Store",
                                    value = storeStr
                                )

                                val curr = account?.currency?.takeIf { it.isNotBlank() } ?: "INR"
                                val currStr = if (curr.equals("INR", ignoreCase = true)) "Indian rupee · ₹" else curr
                                BoldInfoRow(
                                    label = "Currency",
                                    value = currStr
                                )

                                val tz = account?.timezone?.takeIf { it.isNotBlank() } ?: "Asia/Kolkata"
                                val tzStr = if (tz.equals("Asia/Kolkata", ignoreCase = true)) "Asia/Kolkata · IST" else tz
                                BoldInfoRow(
                                    label = "Timezone",
                                    value = tzStr
                                )
                            }
                        }

                        // 4. Notification Preferences Section
                        Text(
                            text = "Notification preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Toggle 1: Low-stock alerts
                                TogglePreferenceRow(
                                    title = "Low-stock alerts",
                                    subtitle = "When stock falls below its minimum",
                                    checked = notifications?.lowStockAlerts ?: true,
                                    onCheckedChange = { viewModel.updateNotificationSetting("low_stock_alerts", it) },
                                    primaryColor = MainBlue
                                )

                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

                                // Toggle 2: AI recommendations
                                TogglePreferenceRow(
                                    title = "AI recommendations",
                                    subtitle = "Reorders, clearance & smart bundles",
                                    checked = notifications?.aiRecommendations ?: true,
                                    onCheckedChange = { viewModel.updateNotificationSetting("ai_recommendations", it) },
                                    primaryColor = MainBlue
                                )

                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

                                // Toggle 3: Daily sales summary
                                TogglePreferenceRow(
                                    title = "Daily sales summary",
                                    subtitle = "Your sales snapshot at 9:00 pm IST",
                                    checked = notifications?.dailySalesSummary ?: true,
                                    onCheckedChange = { viewModel.updateNotificationSetting("daily_sales_summary", it) },
                                    primaryColor = MainBlue
                                )

                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

                                // Toggle 4: Weekly email digest
                                TogglePreferenceRow(
                                    title = "Weekly email digest",
                                    subtitle = "Weekly report sent to your email on Sundays at 8:00 pm IST",
                                    checked = notifications?.weeklyEmailDigest ?: false,
                                    onCheckedChange = { viewModel.updateNotificationSetting("weekly_email_digest", it) },
                                    primaryColor = MainBlue,
                                    enabled = true
                                )
                            }
                        }

                        // 5. Inventory Preferences Section
                        Text(
                            text = "Inventory preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Lead time", style = MaterialTheme.typography.bodySmall, color = MutedInk, fontSize = 12.sp)
                                    Text(
                                        "${inventory?.leadTimeDays ?: 2} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Safety buffer", style = MaterialTheme.typography.bodySmall, color = MutedInk, fontSize = 12.sp)
                                    Text(
                                        "${inventory?.safetyDays ?: 3} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cover days", style = MaterialTheme.typography.bodySmall, color = MutedInk, fontSize = 12.sp)
                                    Text(
                                        "${inventory?.coverDays ?: 7} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Slow-moving window", style = MaterialTheme.typography.bodySmall, color = MutedInk, fontSize = 12.sp)
                                    Text(
                                        "${inventory?.slowMovingThresholdDays ?: 60} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Import mode", style = MaterialTheme.typography.bodySmall, color = MutedInk, fontSize = 12.sp)
                                    val mode = inventory?.importMode ?: "ADD"
                                    val modeStr = if (mode.equals("REPLACE", ignoreCase = true)) "Replace inventory" else "Add received quantities"
                                    Text(
                                        modeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Data & Backup Section
                        Text(
                            text = "Data & backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Product catalog (CSV)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Export all products in your store as a CSV file you can open in Excel or re-import later.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                ExportProductsCsvAction(
                                    isExporting = uiState.isExportingCsv,
                                    exportErrorMessage = uiState.exportErrorMessage,
                                    onConfirmExport = { chosenName -> viewModel.exportProductsCsv(context, chosenName) }
                                )
                            }
                        }

                        // 6. About Section
                        Text(
                            text = "ABOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MainBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BoldInfoRow(
                                    label = "App Name",
                                    value = "StockSense"
                                )

                                BoldInfoRow(
                                    label = "Version",
                                    value = BuildConfig.VERSION_NAME
                                )

                                BoldInfoRow(
                                    label = "Icon Credits",
                                    value = "Icons: Google Noto Emoji, Apache 2.0"
                                )

                                BoldInfoRow(
                                    label = "Avatar Credits",
                                    value = "Avatars by DiceBear. Open Peeps by Pablo Stanley (CC0).\nMicah by Micah Lanier (CC BY 4.0).\nPersonas by Draftbit (CC BY 4.0).",
                                    maxLines = 6
                                )

                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val privacyUrl = "${ApiConstants.BASE_URL}privacy/index.html"
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(privacyUrl)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.util.Log.e("SettingsScreen", "Failed to open privacy policy", e)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Privacy policy",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = InkDark
                                        )
                                        Text(
                                            "Read how your store data and privacy are protected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        "Open ↗",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MainBlue,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        if (BuildConfig.DEBUG) {
                            Text(
                                text = "DEVELOPER / DEBUG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MainBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )

                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = CardWhite,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                onClick = {
                                    if (BuildConfig.DEBUG) {
                                        try {
                                            val intent = android.content.Intent(
                                                context,
                                                Class.forName("com.simats.stocksense.feature.debug.DebugIconGalleryActivity")
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.util.Log.e("SettingsScreen", "Failed to launch icon gallery", e)
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Product Icon Gallery",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = InkDark
                                        )
                                        Text(
                                            "Debug inspector for all 304 vector drawables",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        "View →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MainBlue,
                                        fontWeight = FontWeight.Black
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
                    color = if (enabled) InkDark else MutedInk,
                    fontSize = 13.sp
                )
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftPeach,
                        border = BorderStroke(1.5.dp, InkDark)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = InkDark,
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
                color = MutedInk,
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
                checkedThumbColor = CardWhite,
                checkedTrackColor = MainBlue,
                uncheckedThumbColor = CardWhite,
                uncheckedTrackColor = PaleBlue,
                uncheckedBorderColor = InkDark,
                checkedBorderColor = InkDark,
                disabledUncheckedThumbColor = CardWhite.copy(alpha = 0.6f),
                disabledUncheckedTrackColor = PaleBlue.copy(alpha = 0.5f),
                disabledUncheckedBorderColor = InkDark.copy(alpha = 0.3f),
                disabledCheckedThumbColor = CardWhite.copy(alpha = 0.6f),
                disabledCheckedTrackColor = MainBlue.copy(alpha = 0.5f),
                disabledCheckedBorderColor = InkDark.copy(alpha = 0.3f)
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
