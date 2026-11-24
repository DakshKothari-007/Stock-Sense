package com.simats.stocksense.feature.analytics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.SaleBillDto
import com.simats.stocksense.feature.analytics.model.BillReceiptData
import com.simats.stocksense.feature.analytics.model.BillReceiptItem
import com.simats.stocksense.feature.analytics.viewmodel.SalesAnalyticsViewModel
import com.simats.stocksense.feature.inventory.icon.ProductAvatar

@Composable
fun SalesAnalyticsScreen(
    onBackClick: () -> Unit = {},
    onRecordSaleClick: () -> Unit = {},
    viewModel: SalesAnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MainBlue
    val accentColor = SoftLightBlue
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedBill by remember { mutableStateOf<SaleBillDto?>(null) }

    if (selectedBill != null) {
        val b = selectedBill!!
        val context = LocalContext.current
        val sessionManager = remember { SessionManager.getInstance(context) }
        val shopName = sessionManager.getShopName()?.ifBlank { null }
            ?: sessionManager.getStoreProfile().name.ifBlank { "StockSense Store" }
        val receiptItems = b.items.orEmpty().map { itemDto ->
            BillReceiptItem(
                name = itemDto.displayName,
                quantity = itemDto.quantity ?: 1,
                unitPrice = itemDto.unitPrice ?: 0.0,
                lineTotal = itemDto.lineTotal ?: ((itemDto.quantity ?: 1) * (itemDto.unitPrice ?: 0.0))
            )
        }
        val billData = BillReceiptData(
            shopName = shopName,
            billNo = b.billNo ?: "—",
            dateFormatted = b.saleDate ?: "—",
            items = receiptItems,
            totalAmount = b.totalAmount ?: 0.0,
            totalQuantity = b.totalQuantity ?: receiptItems.sumOf { it.quantity },
            totalItems = b.itemCount ?: receiptItems.size
        )
        BillReceiptScreen(
            bill = billData,
            onRecordAnotherSaleClick = {
                selectedBill = null
                onRecordSaleClick()
            },
            onDoneClick = {
                selectedBill = null
            }
        )
        return
    }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        when {
            uiState.isLoading && uiState.dailySalesPoints.isEmpty() && uiState.todaySalesTotal == "₹0" -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MainBlue,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading sales analytics...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                }
            }

            uiState.errorMessage != null && uiState.dailySalesPoints.isEmpty() && uiState.todaySalesTotal == "₹0" -> {
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
                            text = "Unable to load sales analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                        BoldButton(
                            text = "Retry",
                            onClick = { viewModel.refresh() },
                            isPrimary = true
                        )
                    }
                }
            }

            else -> {
                val windowInfo = rememberWindowSizeInfo()

                ResponsiveContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    maxWidth = 720.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = windowInfo.horizontalPadding)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Header with Shared BoldTopBar & Record sale button
                        val isNarrow = windowInfo.screenWidthDp < 360.dp
                        if (isNarrow) {
                            BoldTopBar(
                                title = "Sales analytics",
                                subtitle = uiState.periodHeaderSubtitle,
                                onBackClick = onBackClick
                            )
                            BoldButton(
                                onClick = onRecordSaleClick,
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF0B1230),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Record sale",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B1230),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            BoldTopBar(
                                title = "Sales analytics",
                                subtitle = uiState.periodHeaderSubtitle,
                                onBackClick = onBackClick,
                                actions = {
                                    BoldButton(
                                        onClick = onRecordSaleClick,
                                        isPrimary = true,
                                        modifier = Modifier.wrapContentWidth(),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color(0xFF0B1230),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Record sale",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0B1230),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            )
                        }

                        // 2. Filter Segmented Chips (Today, 7 days, 30 days)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val periods = listOf("TODAY", "7 DAYS", "30 DAYS")
                            periods.forEachIndexed { index, label ->
                                BoldChip(
                                    text = label,
                                    selected = uiState.selectedPeriodIndex == index,
                                    onClick = { viewModel.selectPeriod(index) },
                                    selectedColor = MainYellow
                                )
                            }
                        }

                        // 3. Metric Summary Cards (Today's sales & Sales 7 days connected)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Today's Sales Card -> Connected to Today button (index 0)
                            val isTodaySelected = uiState.selectedPeriodIndex == 0
                            Box(modifier = Modifier.weight(1f).clickable { viewModel.selectPeriod(0) }) {
                                BoldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = if (isTodaySelected) SoftLavender else CardWhite,
                                    borderColor = InkDark,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Today's sales",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isTodaySelected) InkDark else MutedInk,
                                            fontWeight = if (isTodaySelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = uiState.todaySalesTotal,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = InkDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = uiState.todayBillsText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Sales · 7 days Card -> Connected to 7 days button (index 1)
                            val is7DaysSelected = uiState.selectedPeriodIndex == 1
                            Box(modifier = Modifier.weight(1f).clickable { viewModel.selectPeriod(1) }) {
                                BoldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = if (is7DaysSelected) SoftPeach else CardWhite,
                                    borderColor = InkDark,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Sales · 7 days",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (is7DaysSelected) InkDark else MutedInk,
                                            fontWeight = if (is7DaysSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = uiState.sales7DaysTotal,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = InkDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "7-day total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 30 Days Full Screen Overview Banner -> Connected to 30 days button (index 2)
                        val is30DaysSelected = uiState.selectedPeriodIndex == 2
                        Box(modifier = Modifier.fillMaxWidth().clickable { viewModel.selectPeriod(2) }) {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (is30DaysSelected) PaleBlue else CardWhite,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Sales · 30 days (Full overview)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (is30DaysSelected) MainBlue else MutedInk,
                                            fontWeight = if (is30DaysSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.sales30DaysTotal,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = InkDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Text(
                                        text = uiState.sales30DaysBillsText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // 4. Sales Chart Card (Updates dynamically per period)
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.chartTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.dailySalesPoints.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk
                                        )
                                    }
                                } else {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                    ) {
                                        val totalWidth = maxWidth
                                        val pointCount = uiState.dailySalesPoints.size
                                        val barWidth = if (pointCount > 0) {
                                            ((totalWidth / pointCount) * 0.45f).coerceIn(4.dp, 20.dp)
                                        } else 12.dp

                                        val step = if (pointCount > 14) (pointCount / 5).coerceAtLeast(1)
                                            else if (pointCount > 7 && totalWidth < 360.dp) 2
                                            else 1

                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            uiState.dailySalesPoints.forEachIndexed { index, point ->
                                                val showLabel = (index % step == 0) || (index == pointCount - 1)
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Bottom,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(barWidth)
                                                            .fillMaxHeight(point.heightRatio)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (point.isCurrentDay) MainBlue else SoftLightBlue)
                                                            .border(1.dp, InkDark, RoundedCornerShape(4.dp))
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = if (showLabel) point.dayLabel else " ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MutedInk,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = uiState.chartSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedInk,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.highestDayLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = uiState.highestDayAmount,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // 5. Category Breakdown Card (Updates dynamically per period)
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Category breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 16.sp
                                )

                                if (uiState.categoryBreakdowns.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk
                                        )
                                    }
                                } else {
                                    uiState.categoryBreakdowns.forEach { cat ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedInk,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(0.35f)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .weight(0.35f)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PaleBlue)
                                                    .border(1.dp, InkDark, RoundedCornerShape(4.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(cat.percentage)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MainBlue)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(0.3f)
                                            ) {
                                                Text(
                                                    text = cat.amountText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkDark,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = cat.percentLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedInk,
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Best Sellers · By Revenue Card (Updates dynamically per period)
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Best sellers · by revenue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 16.sp
                                )

                                if (uiState.bestSellers.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk
                                        )
                                    }
                                } else {
                                    uiState.bestSellers.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                ProductAvatar(
                                                    name = item.nameWithUnits,
                                                    category = null,
                                                    size = 32.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = item.nameWithUnits,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = InkDark,
                                                    fontSize = 13.sp,
                                                    lineHeight = 17.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Text(
                                                text = item.revenueText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDark,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.wrapContentWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 7. Recent Bills Card (Tapping a past sale opens the bill receipt)
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
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
                                        text = "Recent bills",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 16.sp
                                    )
                                    if (uiState.bills.isNotEmpty()) {
                                        Text(
                                            text = "${uiState.bills.size} bills",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedInk
                                        )
                                    }
                                }

                                if (uiState.isBillsLoading && uiState.bills.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MainBlue,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else if (uiState.bills.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No sales bills recorded yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        uiState.bills.forEach { bill ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedBill = bill },
                                                color = Color.Transparent
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = bill.billNo ?: "—",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = InkDark,
                                                            fontSize = 14.sp
                                                        )
                                                        Text(
                                                            text = "${bill.saleDate ?: "—"} · ${bill.itemCount ?: 0} items (${bill.totalQuantity ?: 0} units)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MutedInk,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = NumberFormatUtil.formatIndianRupees(bill.totalAmount),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MainBlue,
                                                            fontSize = 14.sp
                                                        )
                                                        Text(
                                                            text = "→",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MutedInk
                                                        )
                                                    }
                                                }
                                            }
                                            HorizontalDivider(
                                                color = InkDark.copy(alpha = 0.08f),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
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

@Preview(showBackground = true)
@Composable
fun SalesAnalyticsScreenPreview() {
    StockSenseTheme {
        SalesAnalyticsScreen()
    }
}
