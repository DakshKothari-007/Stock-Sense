package com.simats.stocksense.feature.avatar.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.avatar.AvatarCatalog
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.avatar.AvatarOptionRow
import com.simats.stocksense.core.avatar.AvatarStyle
import com.simats.stocksense.core.components.BoldButton
import com.simats.stocksense.core.components.BoldCard
import com.simats.stocksense.core.components.BoldChip
import com.simats.stocksense.core.components.BoldDialog
import com.simats.stocksense.core.components.BoldTopBar
import com.simats.stocksense.core.components.UserAvatarBadge
import com.simats.stocksense.core.theme.CardWhite
import com.simats.stocksense.core.theme.CreamBackground
import com.simats.stocksense.core.theme.InkDark
import com.simats.stocksense.core.theme.MainBlue
import com.simats.stocksense.core.theme.MainYellow
import com.simats.stocksense.core.theme.MutedInk
import com.simats.stocksense.core.theme.PaleBlue
import com.simats.stocksense.core.theme.RaisedSurface
import com.simats.stocksense.core.theme.ShadowColor
import com.simats.stocksense.core.theme.SoftPink
import com.simats.stocksense.core.theme.StatusDangerRed
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.theme.ThemeMode
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.avatar.viewmodel.AvatarCustomizerUiState
import com.simats.stocksense.feature.avatar.viewmodel.AvatarCustomizerViewModel
import com.simats.stocksense.feature.avatar.viewmodel.AvatarEditorStep

@Composable
fun AvatarCustomizerScreen(
    onBackClick: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: AvatarCustomizerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initSessionState()
    }

    BackHandler(enabled = uiState.step == AvatarEditorStep.STYLE_IT) {
        viewModel.backToChooseStep()
    }

    if (uiState.showSavedSuccessDialog) {
        BoldDialog(
            onDismissRequest = {
                viewModel.dismissSavedDialog()
                onDone()
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserAvatarBadge(
                    avatarCode = uiState.savedAvatar,
                    displayName = uiState.ownerName,
                    size = 92.dp,
                    borderWidth = 3.dp,
                    fontSize = 30.sp
                )
                Text(
                    text = "Avatar updated",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = InkDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your avatar will appear across Home, Profile, and Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    textAlign = TextAlign.Center
                )
                BoldButton(
                    text = "Continue",
                    onClick = {
                        viewModel.dismissSavedDialog()
                        onDone()
                    },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    AvatarCustomizerContent(
        uiState = uiState,
        onTopBackClick = {
            if (uiState.step == AvatarEditorStep.STYLE_IT) {
                viewModel.backToChooseStep()
            } else {
                onBackClick()
            }
        },
        onStyleTabClick = { viewModel.selectStyleTab(it) },
        onGridSeedClick = { viewModel.selectGridSeed(it) },
        onUseAvatarClick = { viewModel.proceedToStyleStep() },
        onCustomizeCurrentClick = { viewModel.customizeCurrentAvatar() },
        onOptionChange = { key, value -> viewModel.updateSingleOption(key, value) },
        onOptionalVariantChange = { row, value -> viewModel.updateOptionalVariant(row, value) },
        onRandomizeClick = { viewModel.randomizeDraft() },
        onResetClick = { viewModel.resetDraft() },
        onSaveClick = { viewModel.saveAvatar() },
        onRemoveAvatarClick = {
            viewModel.removeAvatar {
                Toast.makeText(context, "Avatar removed", Toast.LENGTH_SHORT).show()
                onDone()
            }
        },
        onDismissError = { viewModel.clearError() }
    )
}

@Composable
fun AvatarCustomizerContent(
    uiState: AvatarCustomizerUiState,
    onTopBackClick: () -> Unit = {},
    onStyleTabClick: (AvatarStyle) -> Unit = {},
    onGridSeedClick: (String) -> Unit = {},
    onUseAvatarClick: () -> Unit = {},
    onCustomizeCurrentClick: () -> Unit = {},
    onOptionChange: (String, String) -> Unit = { _, _ -> },
    onOptionalVariantChange: (AvatarOptionRow.OptionalVariantChips, String?) -> Unit = { _, _ -> },
    onRandomizeClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onRemoveAvatarClick: () -> Unit = {},
    onDismissError: () -> Unit = {}
) {
    val windowInfo = rememberWindowSizeInfo()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 680.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                val title = if (uiState.step == AvatarEditorStep.CHOOSE_AVATAR) {
                    "Choose avatar"
                } else {
                    "Style it"
                }
                val subtitle = if (uiState.step == AvatarEditorStep.CHOOSE_AVATAR) {
                    "STEP 1 OF 2 · PICK A BASE AVATAR"
                } else {
                    "STEP 2 OF 2 · ${uiState.activeStyle.displayName.uppercase()}"
                }

                BoldTopBar(
                    title = title,
                    subtitle = subtitle,
                    onBackClick = onTopBackClick
                )

                if (!uiState.errorMessage.isNullOrBlank()) {
                    BoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        backgroundColor = SoftPink,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusDangerRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                modifier = Modifier
                                    .clickable { onDismissError() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                if (uiState.step == AvatarEditorStep.CHOOSE_AVATAR) {
                    ChooseAvatarStep(
                        uiState = uiState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onStyleTabClick = onStyleTabClick,
                        onGridSeedClick = onGridSeedClick,
                        onUseAvatarClick = onUseAvatarClick,
                        onCustomizeCurrentClick = onCustomizeCurrentClick,
                        onRemoveAvatarClick = onRemoveAvatarClick
                    )
                } else {
                    StyleAvatarStep(
                        uiState = uiState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onOptionChange = onOptionChange,
                        onOptionalVariantChange = onOptionalVariantChange,
                        onRandomizeClick = onRandomizeClick,
                        onResetClick = onResetClick,
                        onSaveClick = onSaveClick,
                        onRemoveAvatarClick = onRemoveAvatarClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseAvatarStep(
    uiState: AvatarCustomizerUiState,
    modifier: Modifier = Modifier,
    onStyleTabClick: (AvatarStyle) -> Unit,
    onGridSeedClick: (String) -> Unit,
    onUseAvatarClick: () -> Unit,
    onCustomizeCurrentClick: () -> Unit,
    onRemoveAvatarClick: () -> Unit
) {
    val presets = remember(uiState.activeStyle) {
        AvatarCatalog.getBasePresets(uiState.activeStyle)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Optional current-avatar card if user already has a saved avatar
        if (uiState.savedAvatar != null) {
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PaleBlue,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 2.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatarBadge(
                            avatarCode = uiState.savedAvatar,
                            displayName = uiState.ownerName,
                            size = 44.dp,
                            borderWidth = 2.dp,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Current avatar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B1230)
                            )
                            Text(
                                text = "Tweak current or pick a new style below",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4A5275),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BoldChip(
                        text = "Edit current",
                        selected = true,
                        selectedColor = MainYellow,
                        onClick = onCustomizeCurrentClick
                    )
                }
            }
        }

        // Style tabs: Open Peeps / Micah / Personas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarStyle.entries.forEach { style ->
                val isSelected = uiState.activeStyle == style
                Surface(
                    onClick = { onStyleTabClick(style) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MainYellow else RaisedSurface,
                    border = BorderStroke(2.dp, InkDark)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color(0xFF0B1230) else InkDark,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 12 base avatars in a 3-column grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(presets, key = { "${uiState.activeStyle.id}_${it.seed}" }) { previewCode ->
                val isSelected = uiState.selectedGridSeed == previewCode.seed

                Box(
                    modifier = Modifier.padding(end = 3.dp, bottom = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Hard shadow under card
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(ShadowColor, RoundedCornerShape(16.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PaleBlue else CardWhite)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MainBlue else InkDark,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onGridSeedClick(previewCode.seed) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarBadge(
                            avatarCode = previewCode,
                            displayName = uiState.ownerName,
                            size = 68.dp,
                            borderWidth = if (isSelected) 2.5.dp else 1.5.dp,
                            fontSize = 20.sp
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MainYellow)
                                    .border(2.dp, InkDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF0B1230),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BoldButton(
                text = "Use avatar",
                onClick = onUseAvatarClick,
                enabled = uiState.selectedGridSeed != null && !uiState.isSaving,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.savedAvatar != null) {
                BoldButton(
                    text = "Remove avatar",
                    onClick = onRemoveAvatarClick,
                    enabled = !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    isPrimary = false,
                    contentColor = StatusDangerRed,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StyleAvatarStep(
    uiState: AvatarCustomizerUiState,
    modifier: Modifier = Modifier,
    onOptionChange: (String, String) -> Unit,
    onOptionalVariantChange: (AvatarOptionRow.OptionalVariantChips, String?) -> Unit,
    onRandomizeClick: () -> Unit,
    onResetClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRemoveAvatarClick: () -> Unit
) {
    val style = AvatarStyle.fromId(uiState.draftAvatar.style) ?: uiState.activeStyle
    val supportedRows = remember(style) { AvatarCatalog.getSupportedRows(style) }

    // Put preview, Randomize/Reset, all style option rows, and Save/Remove inside one scrollable
    // LazyColumn so no option is ever clipped or hidden behind buttons on any screen height.
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp)
    ) {
        item(key = "preview_card") {
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatarBadge(
                        avatarCode = uiState.draftAvatar,
                        displayName = uiState.ownerName,
                        size = 132.dp,
                        borderWidth = 3.dp,
                        fontSize = 36.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BoldButton(
                            text = "Randomize",
                            onClick = onRandomizeClick,
                            enabled = !uiState.isSaving,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                        BoldButton(
                            text = "Reset",
                            onClick = onResetClick,
                            enabled = !uiState.isSaving,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        items(supportedRows, key = { it.title }) { row ->
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 2.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = row.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MainBlue
                    )

                    when (row) {
                        is AvatarOptionRow.ColorSwatches -> {
                            val selectedHex = uiState.draftAvatar.options[row.optionKey]
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(row.colors, key = { it }) { hex ->
                                    ColorSwatchCircle(
                                        hex = hex,
                                        isSelected = selectedHex.equals(hex, ignoreCase = true),
                                        onClick = { onOptionChange(row.optionKey, hex) }
                                    )
                                }
                            }
                        }

                        is AvatarOptionRow.VariantChips -> {
                            val selectedVal = uiState.draftAvatar.options[row.optionKey]
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(row.values, key = { it }) { value ->
                                    BoldChip(
                                        text = AvatarCatalog.formatOptionLabel(value),
                                        isSelected = selectedVal == value,
                                        selectedColor = MainYellow,
                                        onClick = { onOptionChange(row.optionKey, value) }
                                    )
                                }
                            }
                        }

                        is AvatarOptionRow.OptionalVariantChips -> {
                            val prob = uiState.draftAvatar.options[row.probabilityKey]
                            val selectedVar = uiState.draftAvatar.options[row.variantKey]
                            val isNone = prob == "0"
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item(key = "none") {
                                    BoldChip(
                                        text = "None",
                                        isSelected = isNone,
                                        selectedColor = MainYellow,
                                        onClick = { onOptionalVariantChange(row, null) }
                                    )
                                }
                                items(row.values, key = { it }) { value ->
                                    val isItemSelected = !isNone && selectedVar == value
                                    BoldChip(
                                        text = AvatarCatalog.formatOptionLabel(value),
                                        isSelected = isItemSelected,
                                        selectedColor = MainYellow,
                                        onClick = { onOptionalVariantChange(row, value) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "save_actions") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoldButton(
                    text = "Save",
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.savedAvatar != null) {
                    BoldButton(
                        text = "Remove avatar",
                        onClick = onRemoveAvatarClick,
                        enabled = !uiState.isSaving,
                        isPrimary = false,
                        contentColor = StatusDangerRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchCircle(
    hex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isTransparent = hex.equals("transparent", ignoreCase = true)
    val parsedColor = remember(hex) {
        if (isTransparent) {
            Color.Transparent
        } else {
            try {
                Color(("ff" + hex.lowercase()).toLong(16))
            } catch (_: Throwable) {
                Color.LightGray
            }
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MainBlue, CircleShape)
                } else {
                    Modifier.border(2.dp, InkDark, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isTransparent) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                drawLine(
                    color = StatusDangerRed,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isSelected) 3.dp else 0.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
            )
        }

        if (isSelected) {
            val lum = (0.299f * parsedColor.red + 0.587f * parsedColor.green + 0.114f * parsedColor.blue)
            val tickTint = if (isTransparent || lum > 0.6f) Color(0xFF0B1230) else Color.White
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected color",
                tint = tickTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarCustomizerStep1Preview() {
    StockSenseTheme {
        AvatarCustomizerContent(
            uiState = AvatarCustomizerUiState(
                step = AvatarEditorStep.CHOOSE_AVATAR,
                activeStyle = AvatarStyle.OPEN_PEEPS,
                selectedGridSeed = "op_m4x9k2a1",
                ownerName = "Ananya Rao"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarCustomizerStep2DarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        AvatarCustomizerContent(
            uiState = AvatarCustomizerUiState(
                step = AvatarEditorStep.STYLE_IT,
                activeStyle = AvatarStyle.MICAH,
                draftAvatar = AvatarCode(
                    style = AvatarStyle.MICAH.id,
                    seed = "mc_r8k3v2n1",
                    options = mapOf("baseColor" to "f9c9b6", "hairVariant" to "fonze", "backgroundColor" to "fff4dc")
                ),
                ownerName = "Ananya Rao"
            )
        )
    }
}
