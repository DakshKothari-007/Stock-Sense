package com.simats.stocksense.feature.inventory.ui

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.ImportStockPreviewItem
import com.simats.stocksense.feature.inventory.viewmodel.ImportViewModel

@Composable
fun ImportInventoryReadyScreen(
    viewModel: ImportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onImportProductsClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()
    val validationData = uiState.validationData
    val summary = validationData?.summary

    val stockPreviewItems = validationData?.stockPreview?.takeIf { it.isNotEmpty() } ?: listOf(
        ImportStockPreviewItem(name = "Amul Milk", sku = "DAI-001", currentUnits = 8, unitsToAdd = 60, newUnits = 68, displayCalculation = "8 + 60 = 68"),
        ImportStockPreviewItem(name = "Aashirvaad Atta", sku = "STP-003", currentUnits = 24, unitsToAdd = 12, newUnits = 36, displayCalculation = "24 + 12 = 36"),
        ImportStockPreviewItem(name = "Tata Tea", sku = "BEV-008", currentUnits = 42, unitsToAdd = 18, newUnits = 60, displayCalculation = "42 + 18 = 60"),
        ImportStockPreviewItem(name = "Parle-G", sku = "SNK-002", currentUnits = 120, unitsToAdd = 24, newUnits = 144, displayCalculation = "120 + 24 = 144"),
        ImportStockPreviewItem(name = "Surf Excel", sku = "HOM-021", currentUnits = 0, unitsToAdd = 20, newUnits = 20, displayCalculation = "0 + 20 = 20", isNew = true)
    )

    val readyRowsCount = validationData?.readyRows ?: 5
    val totalRowsCount = validationData?.rowsChecked ?: 5
    val existingCount = summary?.existingProductsToUpdate ?: 4
    val newCount = summary?.newProductsToCreate ?: 1
    val totalStockFormatted = summary?.totalStockToAddFormatted ?: "134 units"
    val inventoryValueFormatted = summary?.inventoryValueToAddFormatted ?: "₹7,592"

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

                // 1. Header with Shared BoldTopBar
                BoldTopBar(
                    title = "Ready to import",
                    subtitle = "${uiState.selectedFileName ?: "Spreadsheet"} · Validation complete",
                    onBackClick = onBackClick
                )

                // 2. Stepper Indicators (Upload [done], Preview [done], Validate [done], Import [active])
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Upload", "Preview", "Validate", "Import")
                    steps.forEachIndexed { index, stepName ->
                        val isDone = index < 3
                        val isActive = index == 3
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive || isDone) InkDark else MutedInk,
                                fontWeight = if (isActive || isDone) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isActive || isDone) MainBlue else PaleBlue)
                            )
                        }
                    }
                }

                // 3. Validation Ready / Correction Applied Banner
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PaleBlue,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val iconColor = if (ThemeState.isDark) OnPastelIcon else MainBlue
                            drawCircle(
                                color = iconColor,
                                radius = size.width / 2f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            val path = Path().apply {
                                moveTo(size.width * 0.25f, size.height * 0.5f)
                                lineTo(size.width * 0.45f, size.height * 0.7f)
                                lineTo(size.width * 0.75f, size.height * 0.35f)
                            }
                            drawPath(
                                path = path,
                                color = iconColor,
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (uiState.appliedCorrectionSummary != null) "Correction applied · Zero errors" else "All rows validated · Zero errors",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPastelInk,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.appliedCorrectionSummary ?: "All $readyRowsCount rows are valid and ready to import.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnPastelMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 4. Summary Metric Cards (Ready rows & Errors remaining)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BoldCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Ready rows",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$readyRowsCount / $totalRowsCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }

                    BoldCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Errors remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                // 5. Import Summary Card
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
                        Text(
                            text = "Import summary",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        SummaryRow("Existing products to update", "$existingCount")
                        SummaryRow("New products to create", "$newCount")
                        SummaryRow("Total stock to add", totalStockFormatted)
                        SummaryRow("Inventory value to add", inventoryValueFormatted)
                    }
                }

                // 6. New Stock After Import Card
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
                        Text(
                            text = "New stock after import",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        stockPreviewItems.forEach { item ->
                            SummaryRow(
                                label = "${item.name?.takeIf { it.isNotBlank() } ?: "Product"}${if (item.isNew == true) " · New" else ""}",
                                value = item.displayCalculation ?: ""
                            )
                        }
                    }
                }

                // Error alert if execution failed
                if (uiState.executeError != null) {
                    val isTimeout = uiState.executeError?.contains("timed out", ignoreCase = true) == true ||
                            uiState.executeError?.contains("timeout", ignoreCase = true) == true
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SoftPink,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                drawCircle(color = StatusDangerRed, radius = size.width / 2f, style = Stroke(width = 1.8.dp.toPx()))
                                drawLine(
                                    color = StatusDangerRed,
                                    start = Offset(size.width * 0.5f, size.height * 0.25f),
                                    end = Offset(size.width * 0.5f, size.height * 0.6f),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = StatusDangerRed,
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTimeout) "Connection timed out" else "Import failed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.executeError ?: "An error occurred while importing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusDangerRed,
                                    fontSize = 11.sp
                                )
                            }
                            if (isTimeout) {
                                Spacer(modifier = Modifier.width(8.dp))
                                BoldButton(
                                    text = "Retry",
                                    onClick = {
                                        viewModel.executeImport(
                                            onSuccess = onImportProductsClick,
                                            onError = {}
                                        )
                                    },
                                    isPrimary = true,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }
                    }
                }

                // 7. Footer Note
                Text(
                    text = "Quantities are added, not replaced. Your store inventory will update immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // 8. Import Products Button (disabled while executing to prevent double-tap)
                BoldButton(
                    text = if (uiState.isExecuting) "Importing Products..." else "Import Products",
                    onClick = {
                        viewModel.executeImport(
                            onSuccess = onImportProductsClick,
                            onError = {}
                        )
                    },
                    isPrimary = true,
                    enabled = !uiState.isExecuting,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImportInventoryReadyScreenPreview() {
    StockSenseTheme {
        ImportInventoryReadyScreen()
    }
}
