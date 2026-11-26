package com.simats.stocksense.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.simats.stocksense.feature.inventory.util.ProductCsvExportHelper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.stocksense.core.theme.*

/**
 * BoldCard:
 * - 2.5dp ink border (#111111)
 * - 16dp corner radius
 * - 4dp hard offset shadow (no blur) that does not clip or overflow
 */
@Composable
fun BoldCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardWhite,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = ShadowColor,
    shape: CornerBasedShape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Hard shadow layer (no blur, offset)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Foreground Card Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else Modifier
                )
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * BoldButton:
 * - Yellow primary (#FFC93C), White secondary (#FFFFFF)
 * - 2.5dp ink border + 4dp hard shadow
 * - Pressed state: shadow shrinks from 4dp to 1dp with tactile translation
 */
@Composable
fun BoldButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isPrimary: Boolean = true,
    containerColor: Color = if (isPrimary) MainYellow else CardWhite,
    contentColor: Color = if (isPrimary) Color(0xFF0B1230) else InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = ShadowColor,
    shape: CornerBasedShape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActionable = enabled && !isLoading
    val translationX = if (isPressed && isActionable) (shadowOffset - 1.dp) else 0.dp
    val translationY = if (isPressed && isActionable) (shadowOffset - 1.dp) else 0.dp

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset),
        propagateMinConstraints = true
    ) {
        // Hard shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(if (isActionable) shadowColor else shadowColor.copy(alpha = 0.35f), shape)
        )
        // Foreground button
        Row(
            modifier = Modifier
                .offset(x = translationX, y = translationY)
                .defaultMinSize(minHeight = 44.dp)
                .background(if (isActionable) containerColor else containerColor.copy(alpha = 0.5f), shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = isActionable,
                    onClick = onClick
                )
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                content()
            }
        }
    }
}

/**
 * BoldButton with text label convenience overload
 */
@Composable
fun BoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isPrimary: Boolean = true,
    containerColor: Color = if (isPrimary) MainYellow else CardWhite,
    contentColor: Color = if (isPrimary) Color(0xFF0B1230) else InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = ShadowColor,
    shape: CornerBasedShape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
) {
    BoldButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        isPrimary = isPrimary,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        shadowOffset = shadowOffset,
        shadowColor = shadowColor,
        shape = shape,
        contentPadding = contentPadding
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * BoldChip:
 * - Uppercase, bordered with 2dp ink border
 * - Compact touch-target capable chip for category selection and filters
 */
@Composable
fun BoldChip(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selected: Boolean = false,
    selectedColor: Color = MainYellow,
    backgroundColor: Color = RaisedSurface,
    textColor: Color = if (isSelected || selected) Color(0xFF0B1230) else InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.dp,
    shape: CornerBasedShape = RoundedCornerShape(10.dp),
    onClick: (() -> Unit)? = null
) {
    val chipSelected = isSelected || selected
    val activeBg = if (chipSelected) selectedColor else backgroundColor
    val resolvedTextColor = if (chipSelected && (activeBg == MainYellow || selectedColor == MainYellow) && textColor == InkDark) {
        Color(0xFF0B1230)
    } else {
        textColor
    }
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .background(activeBg, shape)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            ),
            color = resolvedTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * BoldStatusChip:
 * - In stock = lavender (#C9B8FF)
 * - Low stock = orange (#FF9F5A)
 * - Out of stock = red (#FF6B6B)
 * - All outlined with 2dp ink border
 */
@Composable
fun BoldStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val clean = status.trim().lowercase()
    val (bgColor, label) = when {
        clean.contains("low") -> Pair(StatusLowStock, if (status.isNotBlank()) status else "LOW STOCK")
        clean.contains("out") || clean.contains("zero") -> Pair(StatusDangerRed, if (status.isNotBlank()) status else "OUT OF STOCK")
        else -> Pair(StatusInStock, if (status.isNotBlank()) status else "IN STOCK")
    }

    Box(
        modifier = modifier
            .wrapContentWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(2.dp, InkDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = InkDark,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * BoldTextField:
 * - 2dp ink border
 * - 12dp corner radius
 * - High contrast ink text & cursor
 */
@Composable
fun BoldTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    label: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = InkDark
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            isError = isError,
            placeholder = {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk.copy(alpha = 0.7f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RaisedSurface,
                unfocusedContainerColor = RaisedSurface,
                disabledContainerColor = RaisedSurface.copy(alpha = 0.5f),
                errorContainerColor = SoftPink,
                focusedBorderColor = InkDark,
                unfocusedBorderColor = InkDark,
                errorBorderColor = StatusDangerRed,
                focusedTextColor = InkDark,
                unfocusedTextColor = InkDark,
                cursorColor = InkDark
            ),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = StatusDangerRed,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * BoldDialog:
 * - 2.5dp ink border, 16dp radius, 4dp hard shadow
 * - Cream background (#FFF4DC)
 */
@Composable
fun BoldDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        BoldCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            backgroundColor = CreamBackground,
            borderColor = InkDark,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

/**
 * BoldDialog with title, text, confirm and dismiss buttons
 */
@Composable
fun BoldDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: String? = null,
    text: String? = null,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        BoldCard(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            backgroundColor = CreamBackground,
            borderColor = InkDark,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = InkDark
                    )
                }
                if (!text.isNullOrBlank()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkDark
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                    }
                    confirmButton()
                }
            }
        }
    }
}

/**
 * BoldTopBar:
 * - Structural top bar with boxed back navigation (2dp border, 40dp, hard shadow, 48dp touch target)
 * - Bold outline typography and action slots
 */
@Composable
fun BoldTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Hard offset shadow (2dp)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 2.dp, y = 2.dp)
                        .background(ShadowColor, RoundedCornerShape(12.dp))
                )
                // Boxed button (40dp, 2dp border)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardWhite, RoundedCornerShape(12.dp))
                        .border(2.dp, InkDark, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = InkDark
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = InkDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (actions != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/**
 * StockSenseLogo:
 * - Displays official logo asset
 * - In Dark Mode: places the ink symbol on a cream (#FFF4DC) rounded square with 2dp border so it stays visible
 */
@Composable
fun StockSenseLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val isDark = ThemeState.currentBoldColors.isDark
    if (isDark) {
        Box(
            modifier = modifier
                .size(size)
                .background(Color(0xFFFFF4DC), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFFFFF4DC), RoundedCornerShape(12.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.simats.stocksense.R.drawable.stocksense_symbol_inkblue),
                contentDescription = "StockSense Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    } else {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.simats.stocksense.R.drawable.stocksense_symbol_inkblue),
            contentDescription = "StockSense Logo",
            modifier = modifier.size(size),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

/**
 * BoldInfoRow:
 * Shared label/value row component used across Settings and Profile cards.
 * - Fixed-width label on the left (about 88dp), aligned to top of row.
 * - Value on the right taking remaining width (Modifier.weight(1f)), right-aligned.
 * - Values may wrap to 2 lines, staying right-aligned when wrapped while the label stays at the top.
 */
@Composable
fun BoldInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 88.dp,
    maxLines: Int = 2
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.width(labelWidth)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.End,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Shared CSV export button + "Name your file" Bold Outline dialog + inline error banner.
 * Uses rememberSaveable so open dialog state and edited text survive rotation.
 */
@Composable
fun ExportProductsCsvAction(
    isExporting: Boolean,
    exportErrorMessage: String?,
    enabled: Boolean = true,
    onConfirmExport: (chosenBaseName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var fileNameDraft by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExportProductsCsvButton(
            isExporting = isExporting,
            enabled = enabled,
            onClick = {
                fileNameDraft = ProductCsvExportHelper.defaultExportBaseName()
                showNameDialog = true
            }
        )

        if (!exportErrorMessage.isNullOrBlank()) {
            ExportCsvErrorBanner(message = exportErrorMessage)
        }
    }

    if (showNameDialog) {
        ExportFileNameDialog(
            fileNameDraft = fileNameDraft,
            onFileNameChange = { fileNameDraft = it },
            onDismiss = { showNameDialog = false },
            onConfirmExport = { chosenBaseName ->
                showNameDialog = false
                onConfirmExport(chosenBaseName)
            }
        )
    }
}

/**
 * Bold Outline dialog prompting the user to edit the exported CSV filename before downloading.
 */
@Composable
fun ExportFileNameDialog(
    fileNameDraft: String,
    onFileNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmExport: (chosenBaseName: String) -> Unit
) {
    val defaultBaseName = remember { ProductCsvExportHelper.defaultExportBaseName() }
    val sanitizedPreview = remember(fileNameDraft, defaultBaseName) {
        ProductCsvExportHelper.sanitizeExportFileName(fileNameDraft)
    }

    BoldDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Name your file",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = InkDark
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fileNameDraft,
                        onValueChange = onFileNameChange,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = defaultBaseName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { onConfirmExport(sanitizedPreview.baseName) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RaisedSurface,
                            unfocusedContainerColor = RaisedSurface,
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark,
                            cursorColor = InkDark
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PaleBlue,
                        border = BorderStroke(2.dp, InkDark)
                    ) {
                        Text(
                            text = ".csv",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = InkDark,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                        )
                    }
                }

                when {
                    sanitizedPreview.charactersReplaced -> {
                        Text(
                            text = "Some characters were replaced.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                    sanitizedPreview.usedDefault && fileNameDraft.isNotEmpty() -> {
                        Text(
                            text = "Default file name will be used.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BoldButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    isPrimary = false,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.weight(1f)
                )
                BoldButton(
                    text = "Export",
                    onClick = { onConfirmExport(sanitizedPreview.baseName) },
                    isPrimary = true,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Shared Bold Outline button for exporting product inventory as CSV.
 */
@Composable
fun ExportProductsCsvButton(
    isExporting: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoldButton(
        onClick = onClick,
        enabled = enabled && !isExporting,
        isPrimary = false,
        borderWidth = 2.dp,
        shadowOffset = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        if (isExporting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = InkDark,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Exporting CSV...",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = "Export products (CSV)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Shared inline error banner for CSV export errors.
 */
@Composable
fun ExportCsvErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SoftPink,
        border = BorderStroke(1.5.dp, InkDark)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = StatusDangerRed,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

