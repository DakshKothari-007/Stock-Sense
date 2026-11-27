package com.simats.stocksense.feature.inventory.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*

/**
 * Searchable icon picker dialog with department tabs and "Auto" detection option.
 * Searches across English, Hindi, and Tamil names & keywords.
 */
@Composable
fun ProductIconPickerDialog(
    currentIconKey: String?,
    productName: String = "",
    productCategory: String = "",
    onIconSelected: (String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf<ProductDepartment?>(null) }

    val filteredIcons by remember(searchQuery, selectedDepartment) {
        derivedStateOf {
            ProductIconCatalog.search(query = searchQuery, department = selectedDepartment)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            BoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.85f),
                backgroundColor = CreamBackground,
                borderColor = InkDark,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 1. Top Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Choose Product Icon",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Search in English, हिंदी, தமிழ்",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardWhite)
                                .border(2.dp, InkDark, RoundedCornerShape(10.dp))
                                .clickable { onDismissRequest() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search icon (e.g. milk, दूध, பால்)",
                                color = MutedInk.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = InkDark
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = InkDark
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark,
                            cursorColor = InkDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Department Tabs (Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BoldChip(
                            text = "All (${ProductIconCatalog.ITEMS.size})",
                            isSelected = selectedDepartment == null,
                            onClick = { selectedDepartment = null }
                        )

                        ProductDepartment.entries.forEach { dept ->
                            val count = ProductIconCatalog.BY_DEPARTMENT[dept]?.size ?: 0
                            BoldChip(
                                text = "${dept.displayName} ($count)",
                                isSelected = selectedDepartment == dept,
                                onClick = { selectedDepartment = if (selectedDepartment == dept) null else dept }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Auto Option Row
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (currentIconKey == null) SoftLightBlue else CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onIconSelected(null)
                            onDismissRequest()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MainYellow)
                                    .border(2.dp, InkDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = OnPastelIcon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto (Smart Match)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentIconKey == null) OnPastelInk else InkDark
                                )
                                val autoMatched = remember(productName, productCategory) {
                                    ProductIconMatcher.match(productName, productCategory)?.iconKey
                                }
                                val autoHint = if (autoMatched != null) {
                                    "Matches '$autoMatched' from name"
                                } else {
                                    "Detects icon automatically from name"
                                }
                                Text(
                                    text = autoHint,
                                    fontSize = 11.sp,
                                    color = if (currentIconKey == null) OnPastelMuted else MutedInk
                                )
                            }

                            if (currentIconKey == null) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MainBlue)
                                        .border(1.5.dp, InkDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Grid of Icons
                    if (filteredIcons.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No icons found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 68.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(filteredIcons, key = { it.iconKey }) { item ->
                                val isSelected = currentIconKey == item.iconKey

                                BoldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = if (isSelected) MainYellow else CardWhite,
                                    borderColor = InkDark,
                                    borderWidth = if (isSelected) 2.dp else 1.5.dp,
                                    shadowOffset = if (isSelected) 2.dp else 1.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        onIconSelected(item.iconKey)
                                        onDismissRequest()
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        ProductAvatar(
                                            name = item.nameEn,
                                            category = item.department.displayName,
                                            iconKey = item.iconKey,
                                            size = 40.dp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.nameEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) OnPastelInk else InkDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
