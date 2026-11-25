
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
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.fcm.FcmTokenManager
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import androidx.compose.ui.platform.LocalDensity
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
    avatarCode: AvatarCode? = null,
    ownerName: String? = null,
    onAvatarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: (sku: String?, id: Int?) -> Unit = { _, _ -> },
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderPriorityClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
    onFreeUpStockClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
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
        BoldDialog(
            onDismissRequest = {
                showPermissionExplanation = false
                FcmTokenManager.setPromptedPermission(context, true)
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Stock alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    text = "Get alerts when stock runs low.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BoldButton(
                        text = "Not now",
                        onClick = {
                            showPermissionExplanation = false
                            FcmTokenManager.setPromptedPermission(context, true)
                        },
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )
                    BoldButton(
                        text = "Enable",
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
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    )
                }
            }
        }
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
        avatarCode = avatarCode,
        ownerName = ownerName,
        onAvatarClick = onAvatarClick,
        onRefresh = { viewModel.refresh() },
        onNotificationsClick = onNotificationsClick,
        onLowStockClick = onLowStockClick,
        onSlowMovingClick = onSlowMovingClick,
        onAnalyticsClick = onAnalyticsClick,
        onInsightsClick = onInsightsClick,
        onReorderPriorityClick = onReorderPriorityClick,
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
    avatarCode: AvatarCode? = null,
    ownerName: String? = null,
    onAvatarClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: (sku: String?, id: Int?) -> Unit = { _, _ -> },
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderPriorityClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
    onFreeUpStockClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
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
                    avatarCode = avatarCode,
                    ownerName = ownerName,
                    onAvatarClick = onAvatarClick,
                    dailySummary = uiState.dailySummary,
                    unreadCount = uiState.unreadCount,
                    lastUpdatedText = uiState.lastUpdatedText,
                    onNotificationsClick = onNotificationsClick,
                    onLowStockClick = onLowStockClick,
                    onSlowMovingClick = onSlowMovingClick,
                    onAnalyticsClick = onAnalyticsClick,
                    onInsightsClick = onInsightsClick,
                    onReorderPriorityClick = onReorderPriorityClick,
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
    avatarCode: AvatarCode? = null,
    ownerName: String? = null,
    onAvatarClick: () -> Unit = {},
    dailySummary: String? = null,
    unreadCount: Int = 0,
    lastUpdatedText: String = "",
    onNotificationsClick: () -> Unit = {},
    onLowStockClick: (sku: String?, id: Int?) -> Unit = { _, _ -> },
    onSlowMovingClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onReorderPriorityClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
    onFreeUpStockClick: (productId: Int?, sku: String?) -> Unit = { _, _ -> },
    onImportClick: () -> Unit = {}
) {
    val storeName = data.store?.name?.takeIf { it.isNotBlank() } ?: "—"
    val storeLocation = data.store?.location?.takeIf { it.isNotBlank() } ?: "—"
    val todayHeaderDate = NumberFormatUtil.formatCurrentHeaderDate()

    val totalActiveProducts = data.inventory?.totalActiveProducts ?: 0
    val isNewEmptyStore = totalActiveProducts == 0 && (data.sales?.todaySalesAmount ?: 0.0) == 0.0
    val windowInfo = rememberWindowSizeInfo()

    ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 720.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.horizontalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Header Row (Logo, Shop name, location, and notification bell)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StockSenseLogo(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = storeName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = InkDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (todayHeaderDate.isNotEmpty()) "$storeLocation · $todayHeaderDate" else storeLocation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // User Avatar Badge (min 48dp touch target)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarBadge(
                        avatarCode = avatarCode,
                        displayName = ownerName?.takeIf { it.isNotBlank() } ?: storeName,
                        size = 40.dp,
                        borderWidth = 2.dp,
                        fontSize = 14.sp,
                        contentDescription = "User avatar"
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Bell Notification Icon Box (min 48dp touch target)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .border(2.dp, InkDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val w = size.width
                            val h = size.height

                            // Top stem
                            drawLine(
                                color = InkDark,
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
                                color = InkDark,
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
                                color = InkDark,
                                style = Stroke(
                                    width = 1.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Notification badge dot (Shown ONLY when unreadCount > 0)
                            if (unreadCount > 0) {
                                drawCircle(
                                    color = StatusDangerRed,
                                    radius = w * 0.15f,
                                    center = Offset(w * 0.78f, h * 0.20f)
                                )
                                drawCircle(
                                    color = InkDark,
                                    radius = w * 0.15f,
                                    center = Offset(w * 0.78f, h * 0.20f),
                                    style = Stroke(width = 1.2.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            // 2. Metric Cards (Today's Sales & Inventory Value in Soft Boxes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's Sales Card in Soft Peach
                BoldCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SoftPeach
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Today's sales",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPastelInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = NumberFormatUtil.formatIndianRupees(data.sales?.todaySalesAmount),
                            style = MaterialTheme.typography.titleLarge,
                            color = OnPastelInk,
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
                            color = OnPastelMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Inventory Value Card in Soft Light Blue
                BoldCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SoftLightBlue
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Inventory value",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPastelInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = NumberFormatUtil.formatIndianRupees(data.inventory?.totalStockValue),
                            style = MaterialTheme.typography.titleLarge,
                            color = OnPastelInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NumberFormatUtil.formatIndianNumber(totalActiveProducts)} active products",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPastelMuted,
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
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SoftLavender
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to StockSense!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnPastelInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Import your stock to get started with inventory tracking, low-stock alerts, and AI insights.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkDark.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        BoldButton(
                            text = "Import Stock",
                            onClick = onImportClick,
                            isPrimary = true
                        )
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
                // Low Stock Alert Card in Soft Lavender
                val isLowStockActive = lowStockCount > 0 && topLowItem != null
                BoldCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (isLowStockActive) SoftLavender else CardWhite,
                    onClick = {
                        val topId = parseProductId(topLowItem?.id)
                        onLowStockClick(topLowItem?.sku, topId)
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LOW STOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowStockActive) (if (ThemeState.isDark) Color(0xFF0B1230) else StatusLowStock) else MutedInk,
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
                                        color = if (isLowStockActive) OnPastelInk else InkDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Below $lowMin-unit minimum",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isLowStockActive) OnPastelMuted else MutedInk,
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
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All stock levels healthy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Slow Moving Alert Card in Soft Pink
                val isSlowMovingActive = hasSalesHistory && slowMovingCount > 0 && topSlowItem != null
                BoldCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (isSlowMovingActive) SoftPink else CardWhite,
                    onClick = onSlowMovingClick
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SLOW MOVING",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSlowMovingActive) {
                                if (ThemeState.isDark) Color(0xFFFFF4DC) else StatusDangerRed
                            } else MutedInk,
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
                                    color = if (isSlowMovingActive && ThemeState.isDark) Color(0xFFFFF4DC) else InkDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = slowMovingMetadata?.message ?: "Requires 30 days history",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSlowMovingActive && ThemeState.isDark) Color(0xFFFFF4DC).copy(alpha = 0.85f) else MutedInk,
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
                                            color = if (ThemeState.isDark) Color(0xFFFFF4DC) else InkDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$slowUnits left · $slowSold sold / 30 days",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (ThemeState.isDark) Color(0xFFFFF4DC).copy(alpha = 0.85f) else MutedInk,
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
                                    color = InkDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "No slow-moving inventory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
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

            BoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAnalyticsClick() },
                backgroundColor = CardWhite
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
                            color = InkDark,
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
                                color = MainBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftLightBlue,
                                border = BorderStroke(1.5.dp, InkDark)
                            ) {
                                Text(
                                    text = weekDateRangeTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnPastelInk,
                                    fontWeight = FontWeight.Bold,
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
                        color = InkDark,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (chartPoints.isNotEmpty()) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val availableWidth = maxWidth
                            val pointCount = chartPoints.size
                            val columnWidth = if (pointCount > 0) availableWidth / pointCount else availableWidth

                            val formattedLabels = remember(chartPoints) {
                                chartPoints.map { point ->
                                    formatChartDayLabel(point.dayLabel, point.date)
                                }
                            }

                            val fontScale = LocalDensity.current.fontScale
                            val isNarrow = columnWidth < 46.dp || fontScale > 1.2f
                            val isVeryNarrow = columnWidth < 36.dp || (columnWidth < 42.dp && fontScale > 1.3f)
                            val barWidth = (columnWidth * 0.42f).coerceIn(8.dp, 20.dp)

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

                                    val displayLabel = when {
                                        isVeryNarrow -> if (index == 0 || index == pointCount / 2 || index == pointCount - 1) rawAbbr else ""
                                        isNarrow -> if (index % 2 == 0 || index == pointCount - 1) rawAbbr else ""
                                        else -> rawAbbr
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
                                                    .width(barWidth)
                                                    .height((60.dp * ratio).coerceAtLeast(6.dp))
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                    .background(if (isCurrent) MainBlue else SoftLightBlue)
                                                    .border(1.dp, InkDark, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = displayLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isCurrent) MainBlue else MutedInk,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = if (isNarrow) 9.sp else 11.sp,
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
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$weekDateRangeTag · Daily sales in ₹",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk,
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
                        color = InkDark,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "All insights ›",
                        style = MaterialTheme.typography.bodySmall,
                        color = MainBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onInsightsClick() }
                    )
                }

                if (aiPriorities.isNotEmpty()) {
                    aiPriorities.forEach { priority ->
                        AiPriorityCard(
                            priority = priority,
                            primaryColor = MainBlue,
                            onClick = {
                                val productId = parseProductId(priority.productId)
                                val sku = priority.sku?.trim()?.takeIf { it.isNotBlank() }
                                if (priority.type.equals("reorder", ignoreCase = true)) {
                                    onReorderPriorityClick(productId, sku)
                                } else {
                                    onFreeUpStockClick(productId, sku)
                                }
                            }
                        )
                    }
                } else {
                    BoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInsightsClick() },
                        backgroundColor = CardWhite
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 76.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SparkleIcon(primaryColor = MainBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All systems optimal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "AI will alert you when reordering or clearance is recommended",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Daily AI Summary Card (shown only if available)
            if (!dailySummary.isNullOrBlank()) {
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CardWhite
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
                                    .border(1.5.dp, InkDark, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Daily summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = dailySummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkDark,
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
                color = MutedInk,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

/**
 * Safely converts an Any? product ID (e.g., Gson Double 42.0, Int, Long, or String) to a positive Int?.
 */
internal fun parseProductId(rawId: Any?): Int? {
    val parsed = when (rawId) {
        is Int -> rawId
        is Long -> rawId.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
        is Number -> {
            val d = rawId.toDouble()
            if (d.isFinite() && d % 1.0 == 0.0 && d <= Int.MAX_VALUE && d >= Int.MIN_VALUE) d.toInt() else null
        }
        is String -> {
            val trimmed = rawId.trim()
            trimmed.toIntOrNull() ?: trimmed.toDoubleOrNull()?.let { d ->
                if (d.isFinite() && d % 1.0 == 0.0 && d <= Int.MAX_VALUE && d >= Int.MIN_VALUE) d.toInt() else null
            }
        }
        else -> null
    }
    return parsed?.takeIf { it > 0 }
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
    BoldCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 76.dp),
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
                        color = InkDark,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    if (!priority.subtitle.isNullOrBlank()) {
                        Text(
                            text = priority.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
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
                    color = InkDark,
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
            .background(SoftLavender)
            .border(1.5.dp, InkDark, CircleShape),
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
                color = InkDark,
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

@Preview(name = "Dashboard Dark Mode", showBackground = true)
@Composable
fun DashboardHomeScreenDarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}

@Preview(name = "Dashboard Dark 360dp Large Font", showBackground = true, widthDp = 360, fontScale = 1.3f)
@Composable
fun DashboardHomeScreenDarkSmallFontPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}
