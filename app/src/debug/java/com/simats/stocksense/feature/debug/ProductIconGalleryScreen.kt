package com.simats.stocksense.feature.debug

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.theme.ThemeMode
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.inventory.icon.ProductDepartment
import com.simats.stocksense.feature.inventory.icon.ProductIconCatalog
import com.simats.stocksense.feature.inventory.icon.ProductIconItem

/**
 * Debug-only gallery screen to inspect and verify all 304 product vector drawables.
 * Renders each icon on both a plain background and inside the tinted ProductAvatar box,
 * in both Light and Dark mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductIconGalleryScreen(
    onBackClick: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf<ProductDepartment?>(null) }
    var previewMode by remember { mutableStateOf(0) } // 0 = Both, 1 = Plain Only, 2 = Avatar Box Only

    val themeMode = if (isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT

    StockSenseTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Product Icon Gallery",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "304 retail icons (Noto Emoji vectors)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { paddingValues ->
                val allItems = ProductIconCatalog.ITEMS
                val filteredItems = remember(searchQuery, selectedDepartment) {
                    val query = searchQuery.trim().lowercase()
                    allItems.filter { item ->
                        val matchesDept = selectedDepartment == null || item.department == selectedDepartment
                        val matchesQuery = query.isEmpty() ||
                                item.iconKey.contains(query) ||
                                item.nameEn.lowercase().contains(query) ||
                                item.nameHi.lowercase().contains(query) ||
                                item.nameTa.lowercase().contains(query) ||
                                item.keywords.any { it.contains(query) }
                        matchesDept && matchesQuery
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search by key, name, brand, or term...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Filter chips: Department tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDepartment == null,
                            onClick = { selectedDepartment = null },
                            label = { Text("All (${allItems.size})") }
                        )
                        ProductDepartment.values().forEach { dept ->
                            val count = ProductIconCatalog.BY_DEPARTMENT[dept]?.size ?: 0
                            FilterChip(
                                selected = selectedDepartment == dept,
                                onClick = {
                                    selectedDepartment = if (selectedDepartment == dept) null else dept
                                },
                                label = { Text("${dept.displayName} ($count)") }
                            )
                        }
                    }

                    // Display mode switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredItems.size} icons showing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Both", "Plain", "Avatar Box").forEachIndexed { idx, title ->
                                val selected = previewMode == idx
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    onClick = { previewMode = idx }
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Grid of icons
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 145.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.iconKey }) { item ->
                            IconGalleryCard(item = item, previewMode = previewMode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconGalleryCard(
    item: ProductIconItem,
    previewMode: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icon rendering area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Plain background
                if (previewMode == 0 || previewMode == 1) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.drawableRes),
                            contentDescription = item.iconKey,
                            modifier = Modifier.size(30.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                if (previewMode == 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // 2. Avatar tinted box
                if (previewMode == 0 || previewMode == 2) {
                    ProductAvatar(
                        name = item.nameEn,
                        category = item.department.displayName,
                        iconKey = item.iconKey,
                        size = 46.dp
                    )
                }
            }

            // Key label
            Text(
                text = item.iconKey,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // English name
            Text(
                text = item.nameEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Department badge
            Surface(
                color = item.department.lightBg.copy(alpha = 0.8f),
                shape = CircleShape
            ) {
                Text(
                    text = item.department.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = item.department.badgeTextColor,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
