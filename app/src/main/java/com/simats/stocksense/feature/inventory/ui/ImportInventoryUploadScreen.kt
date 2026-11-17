package com.simats.stocksense.feature.inventory.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
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
fun ImportInventoryUploadScreen(
    viewModel: ImportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPreviewImportClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            var fileName = "selected_sheet.csv"
            var fileSize = "File selected · Ready for import"
            try {
                val cursor = context.contentResolver.query(selectedUri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            val displayName = c.getString(nameIndex)
                            if (!displayName.isNullOrEmpty()) {
                                fileName = displayName
                            }
                        }
                        if (sizeIndex != -1) {
                            val bytes = c.getLong(sizeIndex)
                            if (bytes > 0) {
                                fileSize = "${bytes / 1024} KB · Ready for import"
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            viewModel.setSelectedFile(selectedUri, fileName, fileSize)
        }
    }

    val allowedMimeTypes = remember {
        arrayOf(
            "text/csv",
            "text/comma-separated-values",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/csv",
            "*/*"
        )
    }

    val isFileChosen = uiState.selectedUri != null

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
                    title = "Import inventory",
                    subtitle = "Add stock from a spreadsheet",
                    onBackClick = onBackClick
                )

                // 2. Stepper Indicators (Upload, Preview, Validate, Import)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Upload", "Preview", "Validate", "Import")
                    steps.forEachIndexed { index, stepName ->
                        val isActive = index == 0
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) InkDark else MutedInk,
                                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isActive) MainBlue else PaleBlue)
                            )
                        }
                    }
                }

                // 3. Stock Sheet Drop/Upload Area Card (Tappable)
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CardWhite,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    onClick = { filePickerLauncher.launch(allowedMimeTypes) },
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Start with your stock sheet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "CSV or Excel (.xlsx) · Up to 5 MB · One product per row",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFileChosen) PaleBlue else SoftLavender,
                            border = BorderStroke(1.5.dp, InkDark)
                        ) {
                            Text(
                                text = if (isFileChosen) "File selected · Tap to change" else "Tap here to select file",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 4. File Selected Details Card
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CardWhite,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = uiState.selectedFileName ?: "No file selected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isFileChosen) InkDark else MutedInk,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.selectedFileSize ?: "Select a .csv, .xls, or .xlsx file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Upload Error Alert Card (if any error occurred)
                if (uiState.uploadError != null) {
                    val isTimeout = uiState.uploadError?.contains("timed out", ignoreCase = true) == true ||
                            uiState.uploadError?.contains("timeout", ignoreCase = true) == true
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
                                    text = if (isTimeout) "Connection timed out" else "Upload error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDangerRed,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.uploadError ?: "An error occurred while uploading file.",
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
                                        viewModel.uploadAndPreview(
                                            context = context,
                                            onSuccess = onPreviewImportClick,
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

                // 5. Your sheet should include Card
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
                            text = "Your sheet should include",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 14.sp
                        )

                        RequirementRow("Product identity", "SKU + Product name")
                        RequirementRow("Stock receipt", "Quantity to add")
                        RequirementRow("Prices in ₹", "Cost + Selling price")

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Existing SKUs gain stock. A new SKU creates a product. Nothing is overwritten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 6. Preview Import Button
                BoldButton(
                    text = if (uiState.isUploading) "Uploading & Parsing..." else "Preview Import",
                    onClick = {
                        viewModel.uploadAndPreview(
                            context = context,
                            onSuccess = onPreviewImportClick,
                            onError = {}
                        )
                    },
                    isPrimary = true,
                    enabled = isFileChosen && !uiState.isUploading,
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. Recent Imports / History Card
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
                            Text(
                                text = "Recent Imports",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 14.sp
                            )
                            if (uiState.isLoadingHistory) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MainBlue,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        if (!uiState.isLoadingHistory && uiState.historyList.isEmpty()) {
                            Text(
                                text = "No imports yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            uiState.historyList.take(5).forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.fileName?.takeIf { it.isNotBlank() } ?: "Spreadsheet",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = InkDark,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = item.createdAt ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedInk,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Text(
                                        text = "${item.successCount ?: 0} / ${item.totalRows ?: 0} rows",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.status == "completed") MainBlue else MutedInk,
                                        fontSize = 11.sp
                                    )
                                }
                                if (index < minOf(uiState.historyList.size, 5) - 1) {
                                    HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }

                // 8. Cancel - Back to Inventory Text Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { onBackClick() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel · Back to Inventory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RequirementRow(label: String, value: String) {
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
fun ImportInventoryUploadScreenPreview() {
    StockSenseTheme {
        ImportInventoryUploadScreen()
    }
}
