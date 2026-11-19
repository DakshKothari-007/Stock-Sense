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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.inventory.viewmodel.ImportViewModel

@Composable
fun InventoryImportedScreen(
    viewModel: ImportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()
    val execResult = uiState.executionResult

    val fileName = execResult?.fileName ?: uiState.selectedFileName ?: "Spreadsheet"
    val productsUpdated = execResult?.productsUpdated ?: 4
    val productsAdded = execResult?.productsAdded ?: 1
    val dataRowsImported = execResult?.dataRowsImported ?: "5 of 5"
    val stockAdded = execResult?.stockAdded ?: "134 units"
    val skippedRowsCount = execResult?.skippedRows ?: execResult?.skippedCount ?: 0
    val outOfStockProducts = execResult?.outOfStockProducts ?: emptyList()
    val correctedSkipped = execResult?.correctedSkippedText ?: "1 / 0 rows"
    val totalActiveProducts = execResult?.totalActiveProducts?.toString() ?: "129"
    val newInventoryValue = execResult?.newInventoryValue ?: "₹1,56,212"

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
                    title = "Import complete",
                    subtitle = "$fileName · Complete",
                    onBackClick = onBackClick
                )

                // 2. Stepper Indicators (All 4 complete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Upload", "Preview", "Validate", "Import")
                    steps.forEach { stepName ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.labelSmall,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MainBlue)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Success Icon & Headline
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftLavender)
                            .border(2.dp, InkDark, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            drawCircle(
                                color = InkDark,
                                radius = size.width / 2f,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                            val path = Path().apply {
                                moveTo(size.width * 0.25f, size.height * 0.5f)
                                lineTo(size.width * 0.45f, size.height * 0.7f)
                                lineTo(size.width * 0.75f, size.height * 0.35f)
                            }
                            drawPath(
                                path = path,
                                color = InkDark,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Text(
                        text = "Your shelves are up to date.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = InkDark,
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        lineHeight = 30.sp
                    )

                    Text(
                        text = "$fileName · Import complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }

                // 4. Metric Summary Cards
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
                                text = "Products updated",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$productsUpdated",
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
                                text = "Product added",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$productsAdded",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                // 4b. Skipped Rows Banner (if any rows were skipped)
                if (skippedRowsCount > 0) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SoftPeach,
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
                                drawCircle(
                                    color = StatusLowStock,
                                    radius = size.width / 2f,
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                                drawLine(
                                    color = StatusLowStock,
                                    start = Offset(size.width * 0.5f, size.height * 0.25f),
                                    end = Offset(size.width * 0.5f, size.height * 0.6f),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = StatusLowStock,
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (skippedRowsCount == 1) "1 row was skipped" else "$skippedRowsCount rows were skipped",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$dataRowsImported imported. Skipped invalid rows were not added to inventory.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 4c. Out-of-Stock Products Created Card (if any new products created with 0 units)
                if (outOfStockProducts.isNotEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Out-of-stock products created",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftPink,
                                    border = BorderStroke(1.dp, InkDark)
                                ) {
                                    Text(
                                        text = "${outOfStockProducts.size} item${if (outOfStockProducts.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusDangerRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "These products were created with 0 units and are marked Out of Stock.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(InkDark.copy(alpha = 0.15f))
                            )

                            outOfStockProducts.forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name?.takeIf { it.isNotBlank() } ?: "Product",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = InkDark,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${prod.sku?.takeIf { it.isNotBlank() } ?: "SKU"} · ${prod.category?.takeIf { it.isNotBlank() } ?: "General"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SoftPink,
                                        border = BorderStroke(1.dp, InkDark)
                                    ) {
                                        Text(
                                            text = "0 units · Out of stock",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusDangerRed,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Statistics Card
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
                        StatRow("Data rows imported", dataRowsImported)
                        StatRow("Stock added", stockAdded)
                        if (skippedRowsCount > 0) {
                            StatRow("Rows skipped", "$skippedRowsCount")
                        }
                        if (outOfStockProducts.isNotEmpty()) {
                            StatRow("Out-of-stock created", "${outOfStockProducts.size}")
                        }
                        StatRow("Corrected / skipped", correctedSkipped)
                        StatRow("Total active products", totalActiveProducts)
                        StatRow("New inventory value", newInventoryValue)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 6. Done Button
                BoldButton(
                    text = "Done · View Inventory",
                    onClick = {
                        viewModel.clearSession()
                        onDoneClick()
                    },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. Footer Note
                Text(
                    text = "Inventory successfully updated in your store",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
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
private fun StatRow(label: String, value: String) {
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
fun InventoryImportedScreenPreview() {
    StockSenseTheme {
        InventoryImportedScreen()
    }
}
