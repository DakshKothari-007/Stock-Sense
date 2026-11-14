package com.simats.stocksense.feature.inventory.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.inventory.icon.ProductIconCatalog
import com.simats.stocksense.feature.inventory.icon.ProductIconPickerDialog
import com.simats.stocksense.feature.inventory.viewmodel.ProductFormUiState
import com.simats.stocksense.feature.inventory.viewmodel.ProductFormViewModel

/**
 * Screen 6: Edit Product screen and Screen 7: Delete Confirmation Bottom Sheet
 * matching design and API_DOCUMENT.md Module 3.4 & 3.5.
 * Zero hardcoded product details or mock values.
 */
@Composable
fun EditProductScreen(
    viewModel: ProductFormViewModel,
    onBackClick: () -> Unit = {},
    onSuccess: (returnedSku: String) -> Unit = {},
    onDeleted: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    EditProductScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = { viewModel.updateName(it) },
        onSkuChange = { viewModel.updateSku(it) },
        onCategoryChange = { viewModel.updateCategory(it) },
        onSellingPriceChange = { viewModel.updateSellingPrice(it) },
        onPurchaseCostChange = { viewModel.updatePurchaseCost(it) },
        onStockChange = { viewModel.updateStock(it) },
        onMinStockChange = { viewModel.updateMinStock(it) },
        onIconKeyChange = { viewModel.updateIconKey(it) },
        onDismissBanner = { viewModel.clearBannerError() },
        onDismissDeleteConflict = { viewModel.clearDeleteConflictMessage() },
        onSaveClick = {
            viewModel.saveProduct { savedSku, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                onSuccess(savedSku)
            }
        },
        onDeleteClick = {
            viewModel.deleteProduct { msg ->
                val displayMsg = if (msg.isNotBlank()) msg else "Product deleted"
                Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
                onDeleted()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreenContent(
    uiState: ProductFormUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSkuChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSellingPriceChange: (String) -> Unit,
    onPurchaseCostChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onMinStockChange: (String) -> Unit,
    onIconKeyChange: (String?) -> Unit = {},
    onDismissBanner: () -> Unit,
    onDismissDeleteConflict: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteConflictMessage) {
        if (uiState.deleteConflictMessage != null) {
            showDeleteConfirmDialog = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primaryColor,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Header with Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.7f, size.height * 0.2f)
                                lineTo(size.width * 0.3f, size.height * 0.5f)
                                lineTo(size.width * 0.7f, size.height * 0.8f)
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val subtitleText = listOfNotNull(
                        uiState.sku.takeIf { it.isNotBlank() },
                        uiState.category.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifBlank { "—" }

                    Column {
                        Text(
                            text = "Edit product",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Banner (Backend validation or sales constraint message)
                if (uiState.bannerError != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.bannerError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onDismissBanner) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 0. Product icon selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductAvatar(
                            name = uiState.name,
                            category = uiState.category,
                            iconKey = uiState.iconKey,
                            size = 56.dp,
                            modifier = Modifier.clickable { showIconPicker = true }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val iconLabel = if (uiState.iconKey != null) {
                                ProductIconCatalog.get(uiState.iconKey)?.nameEn ?: "Custom icon"
                            } else {
                                "Auto (matches from name)"
                            }
                            Text(
                                text = "Product icon",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = iconLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { showIconPicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Change", fontSize = 12.sp, color = primaryColor)
                        }
                    }

                    if (showIconPicker) {
                        ProductIconPickerDialog(
                            currentIconKey = uiState.iconKey,
                            productName = uiState.name,
                            productCategory = uiState.category,
                            onIconSelected = onIconKeyChange,
                            onDismissRequest = { showIconPicker = false }
                        )
                    }

                    // 1. Product name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Product name",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Product name",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = uiState.nameError != null,
                            supportingText = uiState.nameError?.let {
                                { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorSupportingTextColor = MaterialTheme.colorScheme.error
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // 2. SKU (Displays existing SKU, does not regenerate)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SKU",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = uiState.sku,
                            onValueChange = onSkuChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "SKU",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = uiState.skuError != null,
                            supportingText = uiState.skuError?.let {
                                { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorSupportingTextColor = MaterialTheme.colorScheme.error
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // 3. Category
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.category,
                                onValueChange = onCategoryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                                placeholder = {
                                    Text(
                                        text = "Choose or type a category",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            val filteredCategories = if (uiState.category.isBlank()) {
                                uiState.categories
                            } else {
                                uiState.categories.filter { it.contains(uiState.category, ignoreCase = true) }
                            }

                            if (filteredCategories.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    filteredCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, fontSize = 14.sp) },
                                            onClick = {
                                                onCategoryChange(cat)
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Selling price (₹) & Purchase cost (₹)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Selling price (₹)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value = uiState.sellingPrice,
                                    onValueChange = onSellingPriceChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "0",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    isError = uiState.priceError != null,
                                    supportingText = uiState.priceError?.let {
                                        { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Purchase cost (₹)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value = uiState.purchaseCost,
                                    onValueChange = onPurchaseCostChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "0",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    isError = uiState.costError != null,
                                    supportingText = uiState.costError?.let {
                                        { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                                )
                            }
                        }

                        // Soft Warning when purchase cost is higher than selling price
                        if (uiState.isCostHigherThanPrice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cost is higher than selling price",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // 5. Stock in shop & Minimum stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Stock in shop",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = uiState.stock,
                                onValueChange = onStockChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "0",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                isError = uiState.stockError != null,
                                supportingText = uiState.stockError?.let {
                                    { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Minimum stock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = uiState.minStock,
                                onValueChange = onMinStockChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "10",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                isError = uiState.minStockError != null,
                                supportingText = uiState.minStockError?.let {
                                    { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                            )
                        }
                    }

                    // 6. Note Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Changing \"Stock in shop\" sets the count you have on the shelf. Sales and received orders change it by themselves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fixed Action Buttons at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Save changes
                    Button(
                        onClick = onSaveClick,
                        enabled = !uiState.isSaving && !uiState.isDeleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save changes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Button 2: Delete product
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = !uiState.isSaving && !uiState.isDeleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deleting...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Delete product",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog (Requirement 7 & 10)
    if (showDeleteConfirmDialog) {
        val productName = uiState.deleteProductName.ifBlank { uiState.name }
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    showDeleteConfirmDialog = false
                }
            },
            title = {
                Text(
                    text = "Delete product?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will remove $productName from your inventory. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                    },
                    enabled = !uiState.isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deleting...",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    enabled = !uiState.isDeleting
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 409 Conflict Dialog (Requirement 9)
    if (uiState.deleteConflictMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteConflict,
            title = {
                Text(
                    text = "Cannot delete product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = uiState.deleteConflictMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissDeleteConflict,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

