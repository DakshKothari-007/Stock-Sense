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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.inventory.icon.ProductIconCatalog
import com.simats.stocksense.feature.inventory.icon.ProductIconPickerDialog
import com.simats.stocksense.feature.inventory.viewmodel.ProductFormUiState
import com.simats.stocksense.feature.inventory.viewmodel.ProductFormViewModel

/**
 * Screen 5: New Product screen matching design and API_DOCUMENT.md Module 3.3.
 * Zero hardcoded product details or mock values.
 */
@Composable
fun AddProductScreen(
    viewModel: ProductFormViewModel,
    onBackClick: () -> Unit = {},
    onSuccess: (returnedSku: String) -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    AddProductScreenContent(
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
        onSaveClick = {
            viewModel.saveProduct { returnedSku, msg ->
                val displayMsg = if (returnedSku.isNotBlank()) {
                    "Product added. SKU: $returnedSku"
                } else {
                    msg.ifBlank { "Product added." }
                }
                Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
                onSuccess(returnedSku)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreenContent(
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
    onSaveClick: () -> Unit
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    val windowInfo = rememberWindowSizeInfo()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            maxWidth = 600.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                // Header with Back Button
                BoldTopBar(
                    title = "New product",
                    subtitle = "Add one product to your shop",
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error Banner
                if (uiState.bannerError != null) {
                    BoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        backgroundColor = SoftPink,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp
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
                                fontWeight = FontWeight.Bold,
                                color = StatusDangerRed,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onDismissBanner) {
                                Text("Dismiss", color = InkDark, fontWeight = FontWeight.Bold)
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
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        val isNarrow = windowInfo.screenWidthDp < 360.dp
                        if (isNarrow) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ProductAvatar(
                                        name = uiState.name,
                                        category = uiState.category,
                                        iconKey = uiState.iconKey,
                                        size = 52.dp,
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
                                            text = "PRODUCT ICON",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = InkDark
                                        )
                                        Text(
                                            text = iconLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                BoldButton(
                                    text = "Change",
                                    onClick = { showIconPicker = true },
                                    isPrimary = false,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductAvatar(
                                    name = uiState.name,
                                    category = uiState.category,
                                    iconKey = uiState.iconKey,
                                    size = 52.dp,
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
                                        text = "PRODUCT ICON",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = InkDark
                                    )
                                    Text(
                                        text = iconLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedInk,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                BoldButton(
                                    text = "Change",
                                    onClick = { showIconPicker = true },
                                    isPrimary = false,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
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
                    BoldTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = "Product name",
                        placeholderText = "Name with size, for example 500 ml",
                        isError = uiState.nameError != null,
                        errorMessage = uiState.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // 2. SKU (optional)
                    BoldTextField(
                        value = uiState.sku,
                        onValueChange = onSkuChange,
                        label = "SKU (optional - leave blank to auto-generate)",
                        placeholderText = "Leave empty to auto-generate",
                        isError = uiState.skuError != null,
                        errorMessage = uiState.skuError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // 3. Category (Suggest existing shop categories, allow custom typing)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = InkDark
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
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedInk.copy(alpha = 0.7f)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardWhite,
                                    unfocusedContainerColor = CardWhite,
                                    focusedBorderColor = MainBlue,
                                    unfocusedBorderColor = InkDark,
                                    focusedTextColor = InkDark,
                                    unfocusedTextColor = InkDark,
                                    cursorColor = InkDark
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
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(CardWhite)
                                        .border(2.dp, InkDark, RoundedCornerShape(12.dp))
                                ) {
                                    filteredCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = cat,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = InkDark
                                                )
                                            },
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
                            BoldTextField(
                                value = uiState.sellingPrice,
                                onValueChange = onSellingPriceChange,
                                label = "Selling price (₹)",
                                placeholderText = "0",
                                isError = uiState.priceError != null,
                                errorMessage = uiState.priceError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            BoldTextField(
                                value = uiState.purchaseCost,
                                onValueChange = onPurchaseCostChange,
                                label = "Purchase cost (₹)",
                                placeholderText = "0",
                                isError = uiState.costError != null,
                                errorMessage = uiState.costError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f)
                            )
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
                                    tint = StatusLowStock,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cost is higher than selling price",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusLowStock,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 5. Opening stock & Minimum stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BoldTextField(
                            value = uiState.stock,
                            onValueChange = onStockChange,
                            label = "Opening stock",
                            placeholderText = "0",
                            isError = uiState.stockError != null,
                            errorMessage = uiState.stockError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        BoldTextField(
                            value = uiState.minStock,
                            onValueChange = onMinStockChange,
                            label = "Minimum stock",
                            placeholderText = "10",
                            isError = uiState.minStockError != null,
                            errorMessage = uiState.minStockError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 6. Info Banner Card
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = PaleBlue,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        shape = RoundedCornerShape(14.dp)
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
                                tint = if (ThemeState.isDark) OnPastelIcon else MainBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "You are alerted when stock falls to the minimum. Many products at once? Use Import instead.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = OnPastelInk,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fixed Save Button at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    BoldButton(
                        onClick = onSaveClick,
                        enabled = !uiState.isSaving,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = InkDark,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Save product",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                ),
                                color = InkDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

