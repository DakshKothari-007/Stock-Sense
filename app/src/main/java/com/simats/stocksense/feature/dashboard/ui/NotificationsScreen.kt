package com.simats.stocksense.feature.dashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.NotificationItem
import com.simats.stocksense.feature.dashboard.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onReorderClick: ((Int?) -> Unit)? = null,
    onInsightsClick: (() -> Unit)? = null,
    onInventoryClick: (() -> Unit)? = null,
    onSalesAnalyticsClick: () -> Unit = {},
    onAmulMilkClick: () -> Unit = {},
    onMarieBiscuitsClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val lifecycleOwner = LocalLifecycleOwner.current

    // Single navigation event state: cleared immediately upon firing to prevent duplicate navigations
    var pendingTarget by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(pendingTarget) {
        val target = pendingTarget
        if (target != null) {
            pendingTarget = null // Clear pending target before invoking
            target()
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

    // Handle session expiration
    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                val shopDisplay = uiState.shopName.ifBlank { "Store" }
                val unreadText = if (uiState.unreadCount == 1) "1 unread" else "${uiState.unreadCount} unread"

                // 1. Header with Shared BoldTopBar
                BoldTopBar(
                    title = "Notifications",
                    subtitle = "$shopDisplay · $unreadText",
                    onBackClick = onBackClick
                )

                // 2. Date Subheading
                val todayFormatted = remember {
                    SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date())
                }
                Text(
                    text = "Today · $todayFormatted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 14.sp
                )

                // Error banner with Retry
                if (uiState.errorMessage != null) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SoftPink,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Failed to retrieve notifications.",
                                color = StatusDangerRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text(
                                    text = "Retry",
                                    color = InkDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Loading state
                if (uiState.isLoading && uiState.notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MainBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (uiState.notifications.isEmpty()) {
                    // Empty state: "You're all caught up"
                    BoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        contentPadding = PaddingValues(vertical = 36.dp, horizontal = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "You're all caught up",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = InkDark,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "No new alerts or recommendations at this time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Live Notification Cards
                    uiState.notifications.forEach { item ->
                        NotificationCard(
                            item = item,
                            primaryColor = primaryColor,
                            onClick = {
                                if (pendingTarget != null) return@NotificationCard

                                val key = item.key ?: ""
                                if (key.isNotBlank()) {
                                    viewModel.markAsRead(key)
                                }

                                pendingTarget = {
                                    when (item.type) {
                                        "low_stock" -> {
                                            if (onReorderClick != null) {
                                                onReorderClick(item.productId)
                                            } else {
                                                onAmulMilkClick()
                                            }
                                        }
                                        "slow_moving" -> {
                                            if (onInsightsClick != null) {
                                                onInsightsClick()
                                            } else {
                                                onMarieBiscuitsClick()
                                            }
                                        }
                                        "sales" -> onSalesAnalyticsClick()
                                        "import" -> {
                                            if (onInventoryClick != null) {
                                                onInventoryClick()
                                            } else {
                                                onBackClick()
                                            }
                                        }
                                        else -> onBackClick()
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Note
                Text(
                    text = "Preferences are in Profile → Settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val isRead = item.isRead == true

    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val iconBg = when (item.type) {
                        "low_stock" -> SoftLowStock
                        "slow_moving" -> SoftPeach
                        "sales" -> SoftLightBlue
                        else -> PaleBlue
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBg)
                            .border(1.5.dp, InkDark, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (item.type) {
                            "low_stock" -> {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawRect(
                                        color = InkDark,
                                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                        size = Size(size.width * 0.7f, size.height * 0.7f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawLine(
                                        color = InkDark,
                                        start = Offset(size.width * 0.5f, size.height * 0.15f),
                                        end = Offset(size.width * 0.5f, size.height * 0.85f),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }
                            "slow_moving" -> {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawCircle(
                                        color = InkDark,
                                        radius = size.width * 0.45f,
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                                    drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.65f, size.height * 0.45f))
                                    drawCircle(color = InkDark, radius = size.width * 0.08f, center = Offset(size.width * 0.45f, size.height * 0.65f))
                                }
                            }
                            "sales" -> {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawLine(color = InkDark, start = Offset(size.width * 0.2f, size.height * 0.8f), end = Offset(size.width * 0.2f, size.height * 0.5f), strokeWidth = 2.dp.toPx())
                                    drawLine(color = InkDark, start = Offset(size.width * 0.5f, size.height * 0.8f), end = Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = 2.dp.toPx())
                                    drawLine(color = InkDark, start = Offset(size.width * 0.8f, size.height * 0.8f), end = Offset(size.width * 0.8f, size.height * 0.4f), strokeWidth = 2.dp.toPx())
                                }
                            }
                            else -> {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawRoundRect(
                                        color = InkDark,
                                        topLeft = Offset(size.width * 0.2f, size.height * 0.15f),
                                        size = Size(size.width * 0.6f, size.height * 0.7f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawLine(color = InkDark, start = Offset(size.width * 0.35f, size.height * 0.35f), end = Offset(size.width * 0.65f, size.height * 0.35f), strokeWidth = 1.5.dp.toPx())
                                    drawLine(color = InkDark, start = Offset(size.width * 0.35f, size.height * 0.50f), end = Offset(size.width * 0.65f, size.height * 0.50f), strokeWidth = 1.5.dp.toPx())
                                    drawLine(color = InkDark, start = Offset(size.width * 0.35f, size.height * 0.65f), end = Offset(size.width * 0.55f, size.height * 0.65f), strokeWidth = 1.5.dp.toPx())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title?.takeIf { it.isNotBlank() } ?: "—",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val timeLabel = formatNotificationTime(item.createdForDate)
                        val statusLabel = if (isRead) "Read" else "Unread"
                        Text(
                            text = "$timeLabel · $statusLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Unread Blue Dot
                if (!isRead) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MainBlue)
                            .border(1.dp, InkDark, CircleShape)
                    )
                }
            }

            Text(
                text = item.body?.takeIf { it.isNotBlank() } ?: "—",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val actionText = when (item.type) {
                    "low_stock" -> "Review reorder recommendation"
                    "slow_moving" -> "Review slow-moving recommendation"
                    "sales" -> "View Sales Analytics"
                    "import" -> "View Inventory"
                    else -> "View details"
                }
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = MainBlue,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                    }
                    drawPath(path = path, color = MainBlue, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }
}

private fun formatNotificationTime(createdForDate: String?): String {
    if (createdForDate.isNullOrBlank()) return "—"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetDate = sdf.parse(createdForDate)
        if (targetDate != null) {
            val todayStr = sdf.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)

            when (createdForDate) {
                todayStr -> "Today"
                yesterdayStr -> "Yesterday"
                else -> SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(targetDate)
            }
        } else {
            createdForDate
        }
    } catch (_: Exception) {
        createdForDate
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    StockSenseTheme {
        NotificationsScreen()
    }
}
