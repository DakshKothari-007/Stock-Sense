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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.data.model.ImportPreviewRow
import com.simats.stocksense.feature.inventory.viewmodel.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportInventoryPreviewScreen(
    viewModel: ImportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onContinueToValidationClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()
    val previewData = uiState.previewData

    val currentMapping = uiState.columnMapping ?: previewData?.mappings ?: emptyMap()
    val availableHeaders = previewData?.allHeaders?.takeIf { it.isNotEmpty() } ?: listOf(
        "Product Name", "SKU", "Category", "Quantity", "Cost Price", "Selling Price", "Minimum Stock"
    )

    val displayedRows = previewData?.previewRows ?: listOf(
        ImportPreviewRow(
            rowNumber = 2,
            name = "Amul Milk - 500 ml",
            sku = "DAI-001",
            skuWithSellPrice = "DAI-001 · Sell ₹30",
            unitsToAdd = "60",
            costPrice = "₹27"
        ),
        ImportPreviewRow(
            rowNumber = 3,
            name = "Aashirvaad Atta - 5 kg",
            sku = "STP-003",
            skuWithSellPrice = "STP-003 · Sell ₹260",
            unitsToAdd = "12",
            costPrice = "₹235"
        ),
        ImportPreviewRow(
            rowNumber = 4,
            name = "Tata Tea - 250 g",
            sku = "BEV-008",
            skuWithSellPrice = "BEV-008 · Sell ₹120",
            unitsToAdd = "180",
            costPrice = "₹100",
            isHighlighted = true
        ),
        ImportPreviewRow(
            rowNumber = 5,
            name = "Parle-G - 80 g",
            sku = "SNK-002",
            skuWithSellPrice = "SNK-002 · Sell ₹10",
            unitsToAdd = "24",
            costPrice = "₹8"
        ),
        ImportPreviewRow(
            rowNumber = 6,
            name = "Surf Excel - 500 g",
            sku = "HOM-021",
            skuWithSellPrice = "HOM-021 · Sell ₹65",
            unitsToAdd = "20",
            costPrice = "₹58"
        )
    )

    val mappedCount = currentMapping.filterValues { it.isNotBlank() }.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header with Back Button
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
                        text = "Preview & Map",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${uiState.selectedFileName ?: "Spreadsheet"} · Preview",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // 2. Stepper Indicators (Upload [complete], Preview [active], Validate, Import)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val steps = listOf("Upload", "Preview", "Validate", "Import")
                steps.forEachIndexed { index, stepName ->
                    val isDone = index == 0
                    val isActive = index == 1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stepName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive || isDone) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isActive || isDone) primaryColor else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }

            // 3. Interactive Column Mapping Card with Dropdowns
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
                    Column {
                        Text(
                            text = "$mappedCount columns mapped",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Verify or adjust column assignments from your spreadsheet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Required Fields
                    ColumnMappingDropdownRow(
                        label = "Product Name",
                        isRequired = true,
                        currentValue = currentMapping["name"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("name", it) }
                    )

                    ColumnMappingDropdownRow(
                        label = "SKU",
                        isRequired = true,
                        currentValue = currentMapping["sku"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("sku", it) }
                    )

                    ColumnMappingDropdownRow(
                        label = "Quantity to add",
                        isRequired = true,
                        currentValue = currentMapping["units"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("units", it) }
                    )

                    // Pricing Fields
                    ColumnMappingDropdownRow(
                        label = "Selling Price (₹)",
                        isRequired = false,
                        currentValue = currentMapping["price"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("price", it) }
                    )

                    ColumnMappingDropdownRow(
                        label = "Unit Cost (₹)",
                        isRequired = false,
                        currentValue = currentMapping["cost_price"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("cost_price", it) }
                    )

                    // Optional Metadata Fields
                    ColumnMappingDropdownRow(
                        label = "Category",
                        isRequired = false,
                        currentValue = currentMapping["category"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("category", it) }
                    )

                    ColumnMappingDropdownRow(
                        label = "Minimum Stock Threshold",
                        isRequired = false,
                        currentValue = currentMapping["minimum_stock"],
                        availableHeaders = availableHeaders,
                        onSelectHeader = { viewModel.updateColumnMapping("minimum_stock", it) }
                    )
                }
            }

            // Validation Error Alert Banner (e.g. if required columns are unmapped)
            if (uiState.validationError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(color = Color(0xFFC62828), radius = size.width / 2f, style = Stroke(width = 1.8.dp.toPx()))
                            drawLine(
                                color = Color(0xFFC62828),
                                start = Offset(size.width * 0.5f, size.height * 0.25f),
                                end = Offset(size.width * 0.5f, size.height * 0.6f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color(0xFFC62828),
                                radius = 1.5.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.75f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mapping required",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.validationError ?: "Required columns must be mapped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828).copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 4. Products Table Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRODUCT / SKU",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "ADD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(45.dp)
                        )
                        Text(
                            text = "COST",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(45.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Product Rows
                    displayedRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = row.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = row.skuWithSellPrice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = row.unitsToAdd,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (row.isHighlighted) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                modifier = Modifier.width(45.dp)
                            )

                            Text(
                                text = row.costPrice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.width(45.dp)
                            )
                        }

                        if (row != displayedRows.last()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // 5. Note text
            Text(
                text = "This is a stock receipt: quantities will be added to the current stock. Validation checks values next.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // 6. Action Button
            Button(
                onClick = onContinueToValidationClick,
                enabled = !uiState.isValidating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (uiState.isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Validating rows...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = "Continue to Validation",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnMappingDropdownRow(
    label: String,
    isRequired: Boolean,
    currentValue: String?,
    availableHeaders: List<String>,
    onSelectHeader: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = currentValue ?: "— Not Mapped —"
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (!currentValue.isNullOrBlank()) {
                Text(
                    text = "Mapped",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            } else if (isRequired) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = if (!currentValue.isNullOrBlank()) FontWeight.Bold else FontWeight.Normal,
                    color = if (!currentValue.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (!isRequired) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "— Not Mapped —",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            onSelectHeader(null)
                            expanded = false
                        }
                    )
                }
                availableHeaders.forEach { header ->
                    val isSelected = header.equals(currentValue, ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Text(
                                header,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSelectHeader(header)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImportInventoryPreviewScreenPreview() {
    StockSenseTheme {
        ImportInventoryPreviewScreen()
    }
}
