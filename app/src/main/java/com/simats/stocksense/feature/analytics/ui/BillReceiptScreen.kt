package com.simats.stocksense.feature.analytics.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.analytics.model.BillReceiptData
import com.simats.stocksense.feature.analytics.model.PhoneNumberValidationResult
import com.simats.stocksense.feature.analytics.util.ReceiptFormatter
import com.simats.stocksense.feature.analytics.util.ReceiptPdfGenerator

@Composable
fun DottedDivider(
    modifier: Modifier = Modifier,
    color: Color = InkDark.copy(alpha = 0.25f),
    dotWidth: Dp = 4.dp,
    dotSpacing: Dp = 3.dp,
    strokeWidth: Dp = 1.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth)
    ) {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dotWidth.toPx(), dotSpacing.toPx()),
            0f
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReceiptScreen(
    bill: BillReceiptData,
    onRecordAnotherSaleClick: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val windowInfo = rememberWindowSizeInfo()
    var showShareSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 560.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                BoldTopBar(
                    title = "Bill receipt",
                    subtitle = bill.shopName.ifBlank { "StockSense" },
                    onBackClick = onDoneClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Paper Receipt Card
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 4.dp,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. Shop Name & Receipt Subtitle
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "RECEIPT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = MutedInk,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bill.shopName.ifBlank { "StockSense Store" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (!bill.gstin.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "GSTIN: ${bill.gstin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MutedInk,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            DottedDivider()

                            // 2. Bill Metadata
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bill No", style = MaterialTheme.typography.bodySmall, color = MutedInk)
                                    Text(
                                        text = bill.billNo,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date & Time", style = MaterialTheme.typography.bodySmall, color = MutedInk)
                                    Text(
                                        text = bill.dateFormatted,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkDark
                                    )
                                }
                            }

                            DottedDivider()

                            // 3. Table Column Headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ITEM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    modifier = Modifier.weight(0.44f)
                                )
                                Text(
                                    text = "QTY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.16f)
                                )
                                Text(
                                    text = "PRICE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.20f)
                                )
                                Text(
                                    text = "TOTAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.20f)
                                )
                            }

                            HorizontalDivider(color = InkDark.copy(alpha = 0.2f), thickness = 1.dp)

                            // 4. Line Items List
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (bill.items.isEmpty()) {
                                    Text(
                                        text = "No items in this bill",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    bill.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = InkDark,
                                                modifier = Modifier.weight(0.44f)
                                            )
                                            Text(
                                                text = item.quantity.toString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = InkDark,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(0.16f)
                                            )
                                            Text(
                                                text = ReceiptFormatter.formatAmount(item.unitPrice),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedInk,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(0.20f)
                                            )
                                            Text(
                                                text = ReceiptFormatter.formatAmount(item.lineTotal),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDark,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(0.20f)
                                            )
                                        }
                                    }
                                }
                            }

                            DottedDivider()

                            // 5. Summary & Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Items & Units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk
                                )
                                Text(
                                    text = "${bill.totalItems} items · ${bill.totalQuantity} units",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = InkDark
                                )
                            }

                            if (bill.bundleDiscount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bundle offer",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue
                                    )
                                    Text(
                                        text = "-${ReceiptFormatter.formatAmount(bill.bundleDiscount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL AMOUNT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Text(
                                    text = ReceiptFormatter.formatAmount(bill.totalAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MainBlue,
                                    fontSize = 22.sp
                                )
                            }

                            DottedDivider()

                            // 6. Thank You Footer
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = bill.thankYouMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MutedInk,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 7. Bottom Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BoldButton(
                        text = "Share receipt",
                        onClick = { showShareSheet = true },
                        isPrimary = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BoldButton(
                                text = "Record another",
                                onClick = onRecordAnotherSaleClick,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BoldButton(
                                text = "Done",
                                onClick = onDoneClick,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Share / Send to Customer Bottom Sheet
    if (showShareSheet) {
        SendToCustomerBottomSheet(
            bill = bill,
            onDismiss = { showShareSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendToCustomerBottomSheet(
    bill: BillReceiptData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            phoneInput = ""
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = CardWhite,
        dragHandle = { BottomSheetDefaults.DragHandle(color = InkDark.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Send receipt to customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    text = "Enter customer's mobile number for direct WhatsApp sharing, or leave empty to open the standard share menu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }

            // Customer Phone Number field (Optional, ephemeral, never stored)
            BoldTextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    phoneError = null
                },
                label = "Customer phone number (Optional)",
                placeholderText = "e.g. 98765 43210",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                errorMessage = phoneError,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MutedInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Primary: Share PDF Button
            BoldButton(
                text = "Share receipt (PDF)",
                isPrimary = true,
                onClick = {
                    val validationResult = ReceiptFormatter.cleanPhoneNumber(phoneInput)
                    when (validationResult) {
                        is PhoneNumberValidationResult.Invalid -> {
                            phoneError = validationResult.errorMessage
                        }
                        is PhoneNumberValidationResult.Valid -> {
                            sharePdfViaWhatsApp(
                                context = context,
                                bill = bill,
                                cleanNumber = validationResult.formattedNumber
                            )
                            phoneInput = "" // drop immediately
                            onDismiss()
                        }
                        is PhoneNumberValidationResult.Empty -> {
                            sharePdfNormal(context = context, bill = bill)
                            phoneInput = "" // drop immediately
                            onDismiss()
                        }
                    }
                }
            )

            // Fallback: Text-only wa.me link
            TextButton(
                onClick = {
                    val validationResult = ReceiptFormatter.cleanPhoneNumber(phoneInput)
                    val targetNumber = when (validationResult) {
                        is PhoneNumberValidationResult.Valid -> validationResult.formattedNumber
                        is PhoneNumberValidationResult.Empty -> null
                        is PhoneNumberValidationResult.Invalid -> {
                            phoneError = validationResult.errorMessage
                            return@TextButton
                        }
                    }

                    shareTextViaWaMe(context = context, bill = bill, phoneNumber = targetNumber)
                    phoneInput = "" // drop immediately
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Share as text link (wa.me)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MainBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun sharePdfViaWhatsApp(context: Context, bill: BillReceiptData, cleanNumber: String) {
    try {
        val (_, uri) = ReceiptPdfGenerator.generateReceiptPdf(context, bill)

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra("jid", "$cleanNumber@s.whatsapp.net")
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Here is your bill from ${bill.shopName}: ${bill.billNo}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(whatsappIntent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Could not open WhatsApp directly. Opening share options...",
            Toast.LENGTH_LONG
        ).show()
        sharePdfNormal(context, bill)
    }
}

private fun sharePdfNormal(context: Context, bill: BillReceiptData) {
    try {
        val (_, uri) = ReceiptPdfGenerator.generateReceiptPdf(context, bill)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Bill Receipt - ${bill.billNo}")
            putExtra(Intent.EXTRA_TEXT, "Bill Receipt from ${bill.shopName} - ${bill.billNo}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Bill Receipt")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share receipt: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareTextViaWaMe(context: Context, bill: BillReceiptData, phoneNumber: String?) {
    try {
        val waMeUrl = ReceiptFormatter.buildWaMeUrl(phoneNumber, bill)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waMeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp link: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

