
package com.simats.stocksense.feature.dashboard.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.simats.stocksense.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.fcm.FcmTokenManager
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.data.model.DashboardAiPriority
import com.simats.stocksense.data.model.DashboardSummaryData
import com.simats.stocksense.feature.dashboard.viewmodel.DashboardUiState
import com.simats.stocksense.feature.dashboard.viewmodel.DashboardViewModel

/**
 * Stateful DashboardHomeScreen that collects StateFlow from DashboardViewModel.
 */
@Composable
fun DashboardHomeScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var showPermissionExplanation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        FcmTokenManager.setPromptedPermission(context, true)
        if (isGranted) {
            FcmTokenManager.registerDeviceTokenAsync(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission && !FcmTokenManager.hasPromptedPermission(context)) {
                showPermissionExplanation = true
            }
        }
    }

    if (showPermissionExplanation) {
        AlertDialog(
            onDismissRequest = {
                showPermissionExplanation = false
                FcmTokenManager.setPromptedPermission(context, true)
            },
            title = {
                Text(
                    text = "Stock alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Get alerts when stock runs low.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionExplanation = false
                        FcmTokenManager.setPromptedPermission(context, true)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } catch (_: Throwable) {
                                // Graceful safety
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionExplanation = false
                        FcmTokenManager.setPromptedPermission(context, true)
                    }
                ) {
                    Text(
                        "Not now",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Auto-refresh when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DashboardHomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onNotificationsClick = onNotificationsClick,
        onLowStockClick = onLowStockClick,
        onSlowMovingClick = onSlowMovingClick,
        onAnalyticsClick = onAnalyticsClick,
        onInsightsClick = onInsightsClick,
        onReorderMilkClick = onReorderMilkClick,
        onFreeUpStockClick = onFreeUpStockClick,
        onImportClick = onImportClick
    )
}

/**
 * Renders the dashboard screen state (Loading, Error with Retry, or Real Dashboard Content).
 */
@Composable
fun DashboardHomeScreenContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                // Initial Loading Spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading dashboard...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.errorMessage != null && uiState.dashboardData == null -> {
                // Error State with Retry Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Unable to load dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.dashboardData != null -> {
                DashboardHomeContent(
                    data = uiState.dashboardData,
                    dailySummary = uiState.dailySummary,
                    unreadCount = uiState.unreadCount,
                    lastUpdatedText = uiState.lastUpdatedText,
                    onNotificationsClick = onNotificationsClick,
                    onLowStockClick = onLowStockClick,
                    onSlowMovingClick = onSlowMovingClick,
                    onAnalyticsClick = onAnalyticsClick,
                    onInsightsClick = onInsightsClick,
                    onReorderMilkClick = onReorderMilkClick,
                    onFreeUpStockClick = onFreeUpStockClick,
                    onImportClick = onImportClick
                )
            }
        }
    }
}

/**
 * Main dashboard content populated with real backend data from GET /dashboard/summary.php.
 */
@Composable
fun DashboardHomeContent(
    data: DashboardSummaryData,
    dailySummary: String? = null,
    unreadCount: Int = 0,
    lastUpdatedText: String = "",
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: () -> Unit = {},
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onFreeUpStockClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = Teal500

    val storeName = data.store?.name?.takeIf { it.isNotBlank() } ?: "—"
    val storeLocation = data.store?.location?.takeIf { it.isNotBlank() } ?: "—"
    val todayHeaderDate = NumberFormatUtil.formatCurrentHeaderDate()

    val totalActiveProducts = data.inventory?.totalActiveProducts ?: 0
    val isNewEmptyStore = totalActiveProducts == 0 && (data.sales?.todaySalesAmount ?: 0.0) == 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Header Row (Shop name, location, and notification bell)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (todayHeaderDate.isNotEmpty()) "$storeLocation · $todayHeaderDate" else storeLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bell Notification Icon Box
            val bellIconColor = MaterialTheme.colorScheme.onSurface
            val bellBadgeColor = primaryColor
            val bellSurfaceColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bellSurfaceColor)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val w = size.width
                    val h = size.height

                    // Top stem
                    drawLine(
                        color = bellIconColor,
                        start = Offset(w * 0.5f, h * 0.10f),
                        end = Offset(w * 0.5f, h * 0.20f),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Bell body
                    val bellPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.20f)
                        cubicTo(w * 0.32f, h * 0.20f, w * 0.22f, h * 0.40f, w * 0.22f, h * 0.60f)
                        lineTo(w * 0.12f, h * 0.76f)
                        lineTo(w * 0.88f, h * 0.76f)
                        lineTo(w * 0.78f, h * 0.60f)
                        cubicTo(w * 0.78f, h * 0.40f, w * 0.68f, h * 0.20f, w * 0.5f, h * 0.20f)
                        close()
                    }
                    drawPath(
                        path = bellPath,
                        color = bellIconColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Bell clapper
                    val clapperPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.82f)
                        cubicTo(w * 0.40f, h * 0.94f, w * 0.60f, h * 0.94f, w * 0.60f, h * 0.82f)
                    }
                    drawPath(
                        path = clapperPath,
                        color = bellIconColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Notification badge dot (Shown ONLY when unreadCount > 0)
                    if (unreadCount > 0) {
                        drawCircle(
                            color = bellBadgeColor,
                            radius = w * 0.15f,
                            center = Offset(w * 0.78f, h * 0.20f)
                        )
                        drawCircle(
                            color = bellSurfaceColor,
                            radius = w * 0.15f,
                            center = Offset(w * 0.78f, h * 0.20f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }

        // 2. Metric Cards (Today's Sales & Inventory Value)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's Sales Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Today's sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NumberFormatUtil.formatIndianRupees(data.sales?.todaySalesAmount),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val bills = data.sales?.todayBillsCount ?: 0
                    Text(
                        text = "${NumberFormatUtil.formatIndianNumber(bills)} bills today",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Inventory Value Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Inventory value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NumberFormatUtil.formatIndianRupees(data.inventory?.totalStockValue),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${NumberFormatUtil.formatIndianNumber(totalActiveProducts)} active products",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Empty Store State Banner for newly registered shops
        if (isNewEmptyStore) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to StockSense!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Import your stock to get started with inventory tracking, low-stock alerts, and AI insights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onImportClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Import Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Alerts Row (Low Stock & Slow Moving)
        val lowStockCount = data.inventory?.lowStockCount ?: 0
        val topLowItem = data.alerts?.lowStockItems?.firstOrNull()

        val slowMovingMetadata = data.alerts?.slowMovingMetadata
        val hasSalesHistory = slowMovingMetadata?.hasEnoughSalesHistory ?: false
        val slowMovingCount = data.inventory?.slowMovingCount ?: 0
        val topSlowItem = data.alerts?.slowMovingItems?.firstOrNull()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Low Stock Alert Card
            val isLowStockActive = lowStockCount > 0 && topLowItem != null
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onLowStockClick() },
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowStockActive) Amber50 else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LOW STOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLowStockActive) Amber800 else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isLowStockActive) {
                        val lowName = topLowItem?.name?.takeIf { it.isNotBlank() } ?: "Item"
                        val lowUnits = topLowItem?.units ?: 0
                        val lowMin = topLowItem?.minStockThreshold ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ProductAvatar(
                                name = topLowItem?.name,
                                category = null,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$lowName · $lowUnits left ›",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isLowStockActive) Amber900 else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Below $lowMin-unit minimum",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLowStockActive) Amber800 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Nothing needs attention",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All stock levels healthy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Slow Moving Alert Card
            val isSlowMovingActive = hasSalesHistory && slowMovingCount > 0 && topSlowItem != null
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSlowMovingClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SLOW MOVING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSlowMovingActive) Amber800 else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    when {
                        !hasSalesHistory -> {
                            Text(
                                text = "Sales history needed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = slowMovingMetadata?.message ?: "Requires 30 days history",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        isSlowMovingActive -> {
                            val slowName = topSlowItem?.name?.takeIf { it.isNotBlank() } ?: "Item"
                            val slowUnits = topSlowItem?.units ?: 0
                            val slowSold = topSlowItem?.soldLast30Days ?: 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ProductAvatar(
                                    name = topSlowItem?.name,
                                    category = null,
                                    size = 28.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$slowName ›",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$slowUnits left · $slowSold sold / 30 days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Nothing needs attention",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "No slow-moving inventory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Sales This Week Card (Chart)
        val rawPoints = data.sales?.dailySalesPoints ?: emptyList()
        // Take maximum of 7 days points
        val chartPoints = if (rawPoints.size > 7) rawPoints.takeLast(7) else rawPoints
        val maxAmount = chartPoints.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0

        val firstLabel = chartPoints.firstOrNull()?.let { formatChartDayLabel(it.dayLabel, it.date) } ?: ""
        val lastLabel = chartPoints.lastOrNull()?.let { formatChartDayLabel(it.dayLabel, it.date) } ?: ""
        val weekDateRangeTag = if (firstLabel.isNotEmpty() && lastLabel.isNotEmpty()) {
            val firstDay = firstLabel.split(" ").firstOrNull() ?: firstLabel
            "$firstDay–$lastLabel"
        } else {
            "Past 7 days"
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onAnalyticsClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales this week",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onAnalyticsClick() }
                    ) {
                        Text(
                            text = "Analytics ›",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = weekDateRangeTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = NumberFormatUtil.formatIndianRupees(data.sales?.weeklySalesTotal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart dynamically rendered from dailySalesPoints (max 7 points)
                if (chartPoints.isNotEmpty()) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val availableWidth = maxWidth
                        val pointCount = chartPoints.size
                        val columnWidth = if (pointCount > 0) availableWidth / pointCount else availableWidth

                        // Format all labels from data
                        val formattedLabels = remember(chartPoints) {
                            chartPoints.map { point ->
                                formatChartDayLabel(point.dayLabel, point.date)
                            }
                        }

                        // Avoid overlap on small phones (360dp / large font):
                        // If column width is narrow (< 45dp) and labels are long (> 3 chars), or width is < 36dp, skip alternating labels
                        val hasLongLabels = formattedLabels.any { it.length > 3 }
                        val shouldSkipLabels = (columnWidth < 45.dp && hasLongLabels) || columnWidth < 36.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            chartPoints.forEachIndexed { index, point ->
                                val amount = point.amount ?: 0.0
                                val ratio = if (maxAmount > 0.0) {
                                    (amount / maxAmount).toFloat().coerceIn(0.08f, 1.0f)
                                } else {
                                    0.08f
                                }
                                val isCurrent = point.isCurrentDay == true
                                val rawAbbr = formattedLabels.getOrElse(index) { "" }

                                val displayLabel = if (shouldSkipLabels) {
                                    if (index % 2 == 0 || index == pointCount - 1) rawAbbr else ""
                                } else {
                                    rawAbbr
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height((60.dp * ratio).coerceAtLeast(6.dp))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                .background(if (isCurrent) primaryColor else accentColor)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = displayLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCurrent) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sales recorded this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$weekDateRangeTag · Daily sales in ₹",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 5. Your AI Priorities Section
        val aiPriorities = data.aiPriorities ?: emptyList()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your AI priorities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = "All insights ›",
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onInsightsClick() }
                )
            }

            if (aiPriorities.isNotEmpty()) {
                aiPriorities.forEach { priority ->
                    AiPriorityCard(
                        priority = priority,
                        primaryColor = primaryColor,
                        onClick = {
                            if (priority.type.equals("reorder", ignoreCase = true)) {
                                onReorderMilkClick()
                            } else {
                                onFreeUpStockClick()
                            }
                        }
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onInsightsClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SparkleIcon(primaryColor = primaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All systems optimal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "AI will alert you when reordering or clearance is recommended",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily AI Summary Card (shown only if available)
        if (!dailySummary.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dukaan_mitra),
                            contentDescription = "Dukaan Mitra",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, primaryColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = dailySummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // 6. Footer (Live store data & real timestamp)
        val footerText = if (lastUpdatedText.isNotBlank()) {
            "Live store data · Updated $lastUpdatedText"
        } else {
            "Live store data"
        }

        Text(
            text = footerText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Reusable AI Priority Card displaying reorder/clearance recommendations.
 */
@Composable
private fun AiPriorityCard(
    priority: DashboardAiPriority,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                SparkleIcon(primaryColor = primaryColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = priority.title ?: "AI Recommendation",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    if (!priority.subtitle.isNullOrBlank()) {
                        Text(
                            text = priority.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right Chevron
            Canvas(modifier = Modifier.size(12.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.1f)
                    lineTo(size.width * 0.8f, size.height * 0.5f)
                    lineTo(size.width * 0.2f, size.height * 0.9f)
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

/**
 * AI Sparkle decorative icon.
 */
@Composable
private fun SparkleIcon(primaryColor: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val path = Path().apply {
                moveTo(cx, 0f)
                quadraticTo(cx, cy, size.width, cy)
                quadraticTo(cx, cy, cx, size.height)
                quadraticTo(cx, cy, 0f, cy)
                quadraticTo(cx, cy, cx, 0f)
            }
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Abbreviates chart labels from the data (e.g., "Monday" -> "Mon", "05 November" -> "5 Nov", "2026-11-05" -> "5 Nov").
 * Does not hardcode dates; formats dynamically from backend data.
 */
private fun formatChartDayLabel(rawLabel: String?, rawDate: String?): String {
    val text = rawLabel?.trim().orEmpty()
    if (text.isNotEmpty()) {
        val lower = text.lowercase(Locale.ENGLISH)
        val dayOfWeekMap = mapOf(
            "monday" to "Mon", "tuesday" to "Tue", "wednesday" to "Wed",
            "thursday" to "Thu", "friday" to "Fri", "saturday" to "Sat", "sunday" to "Sun",
            "mon" to "Mon", "tue" to "Tue", "wed" to "Wed",
            "thu" to "Thu", "fri" to "Fri", "sat" to "Sat", "sun" to "Sun"
        )
        dayOfWeekMap[lower]?.let { return it }

        val monthMap = mapOf(
            "january" to "Jan", "february" to "Feb", "march" to "Mar", "april" to "Apr",
            "may" to "May", "june" to "Jun", "july" to "Jul", "august" to "Aug",
            "september" to "Sep", "october" to "Oct", "november" to "Nov", "december" to "Dec",
            "jan" to "Jan", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr",
            "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep",
            "oct" to "Oct", "nov" to "Nov", "dec" to "Dec"
        )

        val parts = text.split("\\s+".toRegex())
        if (parts.size >= 2) {
            val p0 = parts[0]
            val p1 = parts[1]
            val p0Num = p0.toIntOrNull()
            val p1Num = p1.toIntOrNull()
            val m0 = monthMap[p0.lowercase(Locale.ENGLISH)]
            val m1 = monthMap[p1.lowercase(Locale.ENGLISH)]

            if (p0Num != null && m1 != null) {
                return "$p0Num $m1"
            } else if (p1Num != null && m0 != null) {
                return "$p1Num $m0"
            }
        }

        if (text.length <= 6) {
            return text
        }
    }

    // Fallback: parse rawDate (e.g. "2026-11-05")
    if (!rawDate.isNullOrBlank()) {
        try {
            val dateParts = rawDate.trim().split("-")
            if (dateParts.size == 3) {
                val day = dateParts[2].toIntOrNull()
                val monthIdx = dateParts[1].toIntOrNull()
                val months = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                if (day != null && monthIdx != null && monthIdx in 1..12) {
                    return "$day ${months[monthIdx]}"
                }
            }
        } catch (_: Throwable) {}
    }

    return text.take(6)
}

@Preview(showBackground = true)
@Composable
fun DashboardHomeScreenPreview() {
    StockSenseTheme {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}

@Preview(name = "Small Screen 360dp Large Font", showBackground = true, widthDp = 360, fontScale = 1.3f)
@Composable
fun DashboardHomeScreenSmallDevicePreview() {
    StockSenseTheme {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}
