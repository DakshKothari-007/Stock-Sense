package com.simats.stocksense.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StockSenseBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("Home", "Inventory", "Insights", "Profile")

            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            when (index) {
                                0 -> { // Home Icon
                                    val path = Path().apply {
                                        moveTo(size.width * 0.5f, 0f)
                                        lineTo(size.width, size.height * 0.45f)
                                        lineTo(size.width * 0.85f, size.height * 0.45f)
                                        lineTo(size.width * 0.85f, size.height)
                                        lineTo(size.width * 0.15f, size.height)
                                        lineTo(size.width * 0.15f, size.height * 0.45f)
                                        lineTo(0f, size.height * 0.45f)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                1 -> { // Inventory Boxes Icon
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(0f, 0f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(size.width * 0.58f, 0f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(0f, size.height * 0.58f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        topLeft = Offset(size.width * 0.58f, size.height * 0.58f),
                                        size = Size(size.width * 0.42f, size.height * 0.42f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                2 -> { // Insights Sparkle Icon
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val path = Path().apply {
                                        moveTo(cx, 0f)
                                        quadraticTo(cx, cy, size.width, cy)
                                        quadraticTo(cx, cy, cx, size.height)
                                        quadraticTo(cx, cy, 0f, cy)
                                        quadraticTo(cx, cy, cx, 0f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                                3 -> { // Profile Icon
                                    drawCircle(
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        radius = size.width * 0.25f,
                                        center = Offset(size.width * 0.5f, size.height * 0.3f),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    val bodyPath = Path().apply {
                                        moveTo(0f, size.height)
                                        quadraticTo(size.width * 0.5f, size.height * 0.55f, size.width, size.height)
                                    }
                                    drawPath(
                                        path = bodyPath,
                                        color = if (isSelected) primaryColor else Color.Gray,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
