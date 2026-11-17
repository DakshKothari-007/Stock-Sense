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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
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
    shadowColor: Color = InkDark,
    shape: CornerBasedShape = RoundedCornerShape(16.dp),
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
                ),
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
    isPrimary: Boolean = true,
    containerColor: Color = if (isPrimary) MainYellow else CardWhite,
    contentColor: Color = InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shape: CornerBasedShape = RoundedCornerShape(14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val translationX = if (isPressed && enabled) (shadowOffset - 1.dp) else 0.dp
    val translationY = if (isPressed && enabled) (shadowOffset - 1.dp) else 0.dp

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Hard shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(if (enabled) InkDark else InkDark.copy(alpha = 0.35f), shape)
        )
        // Foreground button
        Row(
            modifier = Modifier
                .offset(x = translationX, y = translationY)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f), shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
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
    isPrimary: Boolean = true,
    containerColor: Color = if (isPrimary) MainYellow else CardWhite,
    contentColor: Color = InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shape: CornerBasedShape = RoundedCornerShape(14.dp)
) {
    BoldButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isPrimary = isPrimary,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        shadowOffset = shadowOffset,
        shape = shape
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
    backgroundColor: Color = if (isSelected) MainYellow else CardWhite,
    textColor: Color = InkDark,
    borderColor: Color = InkDark,
    borderWidth: Dp = 2.dp,
    shape: CornerBasedShape = RoundedCornerShape(10.dp),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .background(backgroundColor, shape)
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
            color = textColor,
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
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite,
                disabledContainerColor = CardWhite.copy(alpha = 0.5f),
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
 * BoldTopBar:
 * - Structural top bar with back navigation and bold outline design
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
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
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
