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
import com.simats.stocksense.data.model.ImportValidationError
import com.simats.stocksense.feature.inventory.viewmodel.ImportViewModel

@Composable
fun ImportInventoryValidationScreen(
    viewModel: ImportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onValidationPassed: () -> Unit = {},
    onImportCompleted: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = Color(0xFFC62828)
    val errorBgColor = Color(0xFFFFEBEE)

    val uiState by viewModel.uiState.collectAsState()
    val validationData = uiState.validationData

    val errors = validationData?.errors ?: listOf(
        ImportValidationError(
            rowNumber = 4,
            field = "units",
            errorType = "QUANTITY_ERROR",
            errorBadge = "ROW 4 · QUANTITY ERROR",
            productName = "Tata Tea · 250 g",
            sku = "BEV-008",
            importedValue = "18O",
            reason = "\"18O\" contains the letter O. Quantity must be a whole number greater than zero.",
            suggestedCorrection = 18,
            suggestedLabel = "18 units"
        )
    )

    val fixableError = errors.firstOrNull { it.suggestedCorrection != null || it.actions?.isNotEmpty() == true }
    val isZeroStockError = fixableError != null && (
        fixableError.errorType == "QUANTITY_ZERO_EXISTING" ||
        (fixableError.field == "units" && fixableError.importedValue == "0") ||
        fixableError.reason?.contains("existing products", ignoreCase = true) == true
    )

    val rowsChecked = validationData?.rowsChecked ?: 5
    val readyRows = validationData?.readyRows ?: 4
    val errorsRemaining = validationData?.errorsRemaining ?: errors.size

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
                    title = if (errorsRemaining == 1) "One value needs a fix" else "$errorsRemaining values need a fix",
                    subtitle = "${uiState.selectedFileName ?: "Spreadsheet"} · Validation",
                    onBackClick = onBackClick
                )

                // 2. Stepper Indicators (Upload [done], Preview [done], Validate [active], Import)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Upload", "Preview", "Validate", "Import")
                    steps.forEachIndexed { index, stepName ->
                        val isDone = index < 2
                        val isActive = index == 2
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

                // 3. Summary Cards (Rows checked & Ready rows)
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
                                text = "Rows checked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$rowsChecked",
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
                                text = "Ready rows",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$readyRows",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                // 4. Error Alert Banner Box
                if (uiState.validationError != null) {
                    val isTimeout = uiState.validationError?.contains("timed out", ignoreCase = true) == true ||
                            uiState.validationError?.contains("timeout", ignoreCase = true) == true
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
                                    text = if (isTimeout) "Connection timed out" else "Validation error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.validationError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusDangerRed,
                                    fontSize = 11.sp
                                )
                            }
                            if (isTimeout && fixableError != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                BoldButton(
                                    text = "Retry",
                                    onClick = {
                                        viewModel.applyCorrection(
                                            error = fixableError,
                                            onSuccess = { ready -> if (ready) onValidationPassed() },
                                            onError = { }
                                        )
                                    },
                                    isPrimary = true,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }
                    }
                }

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
                        Canvas(modifier = Modifier.size(22.dp)) {
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

                        Column {
                            Text(
                                text = "$errorsRemaining invalid row${if (errorsRemaining > 1) "s" else ""} · Import not ready",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusDangerRed,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Correct the values below or re-upload your file.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusDangerRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 5. Dynamic Error Item Cards
                errors.forEach { err ->
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
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftPink,
                                border = BorderStroke(1.dp, InkDark)
                            ) {
                                Text(
                                    text = err.errorBadge?.takeIf { it.isNotBlank() } ?: "ROW ${err.rowNumber ?: 0} · ERROR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusDangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Text(
                                text = err.productName?.takeIf { it.isNotBlank() } ?: "Product",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 18.sp
                            )

                            Text(
                                text = "SKU ${err.sku?.takeIf { it.isNotBlank() } ?: "N/A"} · Excel row ${err.rowNumber ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Imported value",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = err.importedValue?.takeIf { it.isNotBlank() } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = err.reason ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusDangerRed,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 6. Suggested Correction Card (if available)
                if (fixableError != null) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = PaleBlue,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Suggested correction",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPastelInk,
                                fontSize = 14.sp
                            )

                            if (isZeroStockError) {
                                Text(
                                    text = "${fixableError.productName} is an existing product with 0 quantity in the file. StockSense will never invent stock. Choose how to handle this product:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPastelInk,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                BoldButton(
                                    text = if (uiState.isValidating) "Applying..." else "Import with 0 units (out of stock)",
                                    onClick = {
                                        viewModel.importWithZeroUnits(
                                            error = fixableError,
                                            onSuccess = { ready -> if (ready) onValidationPassed() },
                                            onError = { }
                                        )
                                    },
                                    enabled = !uiState.isValidating,
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                BoldButton(
                                    text = if (uiState.isValidating) "Applying..." else "Skip this row",
                                    onClick = {
                                        viewModel.skipRow(
                                            error = fixableError,
                                            onSuccess = { ready -> if (ready) onValidationPassed() },
                                            onError = { }
                                        )
                                    },
                                    enabled = !uiState.isValidating,
                                    isPrimary = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Replace \"${fixableError.importedValue}\" with",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = fixableError.suggestedLabel ?: "${fixableError.suggestedCorrection}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = "The source appears to have an invalid value or stray letter. Apply the fix to re-validate.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                BoldButton(
                                    text = if (uiState.isValidating) "Applying correction..." else "Apply Fix · Use ${fixableError.suggestedLabel ?: fixableError.suggestedCorrection.toString()}",
                                    onClick = {
                                        viewModel.applyCorrection(
                                            error = fixableError,
                                            onSuccess = { ready ->
                                                if (ready) onValidationPassed()
                                            },
                                            onError = { }
                                        )
                                    },
                                    enabled = !uiState.isValidating,
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // 7. Footer Note
                Text(
                    text = "No products have been imported yet. $readyRows valid rows are held until all $rowsChecked are ready, or you can skip invalid rows.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // 8. Skip Invalid Rows and Import the Rest (when readyRows > 0)
                if (readyRows > 0) {
                    BoldButton(
                        text = if (uiState.isExecuting) "Importing valid rows..." else "Skip invalid rows and import the rest ($readyRows ready)",
                        onClick = {
                            viewModel.executeImport(
                                skipInvalidRows = true,
                                onSuccess = onImportCompleted,
                                onError = { }
                            )
                        },
                        enabled = !uiState.isExecuting && !uiState.isValidating,
                        isPrimary = (fixableError == null),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 9. Back / Re-upload Option
                BoldButton(
                    text = "Fix in Spreadsheet · Upload Again",
                    onClick = onBackClick,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImportInventoryValidationScreenPreview() {
    StockSenseTheme {
        ImportInventoryValidationScreen()
    }
}
