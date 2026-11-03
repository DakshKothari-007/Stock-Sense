package com.simats.stocksense.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Liquid Glass Bottom Navigation Bar with Drag-to-Switch gesture.
 * Features:
 * - Rounded capsule with translucent fill and specular vertical gradient
 * - 1 dp edge highlight and multi-layered soft shadow
 * - Horizontal drag-to-switch with smooth finger tracking, clamping, and liquid drop stretch
 * - Real-time tab highlighting and haptic ticks on boundary crossing
 * - Inertial fling support and spring snap-to-tab on release
 * - Full accessibility with semantics and minimum 48 dp touch targets
 */
@Composable
fun StockSenseBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Glass Surface Styling (55% to 70% opacity of surface color)
    val glassFillColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.65f else 0.70f)

    // Subtle vertical light specular gradient
    val glassGradient = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.02f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.08f)
            )
        }
    )

    // Thin 1 dp edge highlight (brighter on top, fading to transparent at bottom)
    val edgeHighlightGradient = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.10f),
                Color.Transparent
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.28f),
                Color.Transparent
            )
        }
    )

    // Outer Glass Capsule (Padded 16 dp horizontally from screen edges so edge back gestures are never blocked)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color(0xFF1B2E24).copy(alpha = 0.08f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.60f) else Color(0xFF1B2E24).copy(alpha = 0.14f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(glassFillColor)
            .background(glassGradient)
            .border(
                width = 1.dp,
                brush = edgeHighlightGradient,
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            val tabCount = 4
            val tabWidth = maxWidth / tabCount
            val tabWidthPx = with(density) { tabWidth.toPx() }
            val maxOffsetPx = tabWidthPx * (tabCount - 1)

            // Drag State
            var isDragging by remember { mutableStateOf(false) }
            var dragDeltaX by remember { mutableFloatStateOf(0f) }
            var hoveredTab by remember { mutableIntStateOf(selectedTab) }

            // Liquid Drop Dynamics: position and stretch animatables
            var previousTab by remember { mutableIntStateOf(selectedTab) }
            var travelDirection by remember { mutableFloatStateOf(1f) }
            val liquidStretch = remember { Animatable(0f) }
            val pillOffsetAnim = remember { Animatable(selectedTab * tabWidthPx) }

            // Sync pill offset when selectedTab changes externally or from release
            LaunchedEffect(selectedTab, tabWidthPx) {
                if (tabWidthPx > 0f && !isDragging) {
                    val targetPx = selectedTab * tabWidthPx
                    if (selectedTab != previousTab) {
                        travelDirection = if (selectedTab > previousTab) 1f else -1f
                        previousTab = selectedTab

                        // Liquid drop stretch & squash cycle on tab switch
                        launch {
                            liquidStretch.snapTo(0f)
                            liquidStretch.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                            )
                            liquidStretch.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.68f,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }

                    if (pillOffsetAnim.value != targetPx) {
                        pillOffsetAnim.animateTo(
                            targetValue = targetPx,
                            animationSpec = spring(
                                dampingRatio = 0.80f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            }

            // Interaction sources for press feedback on all tabs
            val interactionSources = remember { List(4) { MutableInteractionSource() } }
            val isSelectedPressed by interactionSources[selectedTab].collectIsPressedAsState()

            // Press feedback: scales down slightly (~0.94) on press
            val pillPressScale by animateFloatAsState(
                targetValue = if (isSelectedPressed) 0.94f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                label = "pillPressScale"
            )

            // Drag scale: pill scales up slightly (~1.08) while dragging
            val dragScale by animateFloatAsState(
                targetValue = if (isDragging) 1.08f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                label = "dragScale"
            )

            // Liquid deformation math: stretch width in travel/drag direction, squash height
            val stretch = liquidStretch.value
            val dragStretch = if (isDragging) (dragDeltaX / 12f).coerceIn(-0.25f, 0.25f) else 0f

            val dynamicScaleX = dragScale * (1f + abs(dragStretch) + (if (!isDragging) stretch * 0.25f else 0f)) * pillPressScale
            val dynamicScaleY = dragScale * (1f - abs(dragStretch) * 0.5f - (if (!isDragging) stretch * 0.10f else 0f)) * pillPressScale

            val transformOrigin = TransformOrigin(
                pivotFractionX = if (isDragging) {
                    if (dragDeltaX >= 0) 0.15f else 0.85f
                } else {
                    if (travelDirection > 0) 0.15f else 0.85f
                },
                pivotFractionY = 0.5f
            )

            // Liquid pill colors & gradients (luminous primary green tinted liquid)
            val pillFillGradient = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(
                        primaryColor.copy(alpha = 0.32f),
                        primaryColor.copy(alpha = 0.18f)
                    )
                } else {
                    listOf(
                        primaryColor.copy(alpha = 0.22f),
                        primaryColor.copy(alpha = 0.12f)
                    )
                }
            )

            val pillEdgeHighlight = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(
                        primaryColor.copy(alpha = 0.55f),
                        primaryColor.copy(alpha = 0.20f)
                    )
                } else {
                    listOf(
                        primaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.15f)
                    )
                }
            )

            val currentPillOffsetDp = with(density) { pillOffsetAnim.value.toDp() }

            // Liquid Glass Pill Indicator (Follows finger smoothly when dragging, springs when snapping)
            Box(
                modifier = Modifier
                    .offset(x = currentPillOffsetDp)
                    .width(tabWidth)
                    .height(56.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .graphicsLayer {
                        scaleX = dynamicScaleX
                        scaleY = dynamicScaleY
                        this.transformOrigin = transformOrigin
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(pillFillGradient)
                    .border(
                        width = 1.dp,
                        brush = pillEdgeHighlight,
                        shape = RoundedCornerShape(18.dp)
                    )
            )

            // Navigation Tab Items Row with Drag-to-Switch Gesture Handling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(selectedTab, tabWidthPx) {
                        if (tabWidthPx <= 0f) return@pointerInput
                        val touchSlop = viewConfig.touchSlop

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)

                            // Requirement 9: Outer screen edges are protected from gesture blocking
                            if (down.position.x < 0f || down.position.x > size.width.toFloat()) {
                                return@awaitEachGesture
                            }

                            var isDrag = false
                            var totalDragDistance = 0f
                            val velocityTracker = VelocityTracker()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)

                            val startTouchX = down.position.x
                            val initialPillOffset = pillOffsetAnim.value

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (change.changedToUp()) {
                                    change.consume()
                                    if (isDrag) {
                                        isDragging = false
                                        val velocityX = velocityTracker.calculateVelocity().x
                                        val flingThreshold = 700f
                                        val currentOffset = pillOffsetAnim.value

                                        // Requirement 6: Fling detection moves to next/prev tab
                                        val targetTab = when {
                                            velocityX > flingThreshold -> (selectedTab + 1).coerceAtMost(3)
                                            velocityX < -flingThreshold -> (selectedTab - 1).coerceAtLeast(0)
                                            else -> ((currentOffset / tabWidthPx) + 0.5f).toInt().coerceIn(0, 3)
                                        }

                                        dragDeltaX = 0f

                                        // Requirement 3 & 4: Switch tab on release once, do not reload if same tab
                                        if (targetTab != selectedTab) {
                                            onTabSelected(targetTab)
                                            coroutineScope.launch {
                                                pillOffsetAnim.animateTo(
                                                    targetValue = targetTab * tabWidthPx,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.80f,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                pillOffsetAnim.animateTo(
                                                    targetValue = selectedTab * tabWidthPx,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.80f,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        // Requirement 5: Normal tap works with zero drag slop conflict
                                        val tappedIndex = (change.position.x / tabWidthPx).toInt().coerceIn(0, 3)
                                        if (tappedIndex != selectedTab) {
                                            onTabSelected(tappedIndex)
                                        }
                                    }
                                    break
                                } else if (!change.pressed) {
                                    // Requirement 7: Gesture cancellation returns pill to selected tab
                                    if (isDrag) {
                                        isDragging = false
                                        dragDeltaX = 0f
                                        coroutineScope.launch {
                                            pillOffsetAnim.animateTo(
                                                targetValue = selectedTab * tabWidthPx,
                                                animationSpec = spring(
                                                    dampingRatio = 0.80f,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                    break
                                } else {
                                    // Pointer moving
                                    val deltaX = change.position.x - change.previousPosition.x
                                    totalDragDistance += deltaX
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)

                                    if (!isDrag && abs(totalDragDistance) > touchSlop) {
                                        isDrag = true
                                        isDragging = true
                                        hoveredTab = selectedTab
                                    }

                                    if (isDrag) {
                                        change.consume()
                                        dragDeltaX = deltaX
                                        // Requirement 1: Pill follows finger smoothly, clamped to first and last tab
                                        val newOffset = (initialPillOffset + (change.position.x - startTouchX)).coerceIn(0f, maxOffsetPx)

                                        coroutineScope.launch {
                                            pillOffsetAnim.snapTo(newOffset)
                                        }

                                        // Requirement 2: Highlight tab under pill and trigger light haptic tick on crossing
                                        val currentHovered = ((newOffset / tabWidthPx) + 0.5f).toInt().coerceIn(0, 3)
                                        if (currentHovered != hoveredTab) {
                                            hoveredTab = currentHovered
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            }
                        }
                    },
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("Home", "Inventory", "Insights", "Profile")
                val activeHighlightTab = if (isDragging) hoveredTab else selectedTab

                tabs.forEachIndexed { index, label ->
                    val isHighlighted = activeHighlightTab == index
                    val isSelected = selectedTab == index
                    val interactionSource = interactionSources[index]
                    val isPressed by interactionSource.collectIsPressedAsState()

                    // Press feedback for individual tab icon and label
                    val tabPressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1.0f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMedium,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        label = "tabPressScale_$index"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isHighlighted) 1.12f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale_$index"
                    )

                    val translationY by animateFloatAsState(
                        targetValue = if (isHighlighted) (-2f) else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "translationY_$index"
                    )

                    // Color cross-fade for icon & label
                    val iconColor by animateColorAsState(
                        targetValue = if (isHighlighted) primaryColor else inactiveColor,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "iconColor_$index"
                    )

                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isHighlighted) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "dotAlpha_$index"
                    )

                    val dotScale by animateFloatAsState(
                        targetValue = if (isHighlighted) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "dotScale_$index"
                    )

                    // Requirement 8: Accessibility semantics and min 48 dp touch target
                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .height(56.dp)
                            .semantics {
                                this.selected = isSelected
                                this.role = Role.Tab
                                this.contentDescription = label
                                this.onClick {
                                    if (selectedTab != index) {
                                        onTabSelected(index)
                                    }
                                    true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale * tabPressScale
                                scaleY = scale * tabPressScale
                                this.translationY = translationY * density.density
                            }
                        ) {
                            // Animated Top Accent Dot
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .graphicsLayer {
                                        scaleX = dotScale
                                        scaleY = dotScale
                                        alpha = dotAlpha
                                    }
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Icon Canvas
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
                                        if (isHighlighted) {
                                            drawPath(
                                                path = path,
                                                color = iconColor.copy(alpha = 0.25f),
                                                style = Fill
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = iconColor,
                                            style = Stroke(
                                                width = if (isHighlighted) 2.2.dp.toPx() else 1.8.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                    1 -> { // Inventory Boxes Icon
                                        val strokeWidth = if (isHighlighted) 2.2.dp.toPx() else 1.8.dp.toPx()
                                        val boxes = listOf(
                                            Offset(0f, 0f),
                                            Offset(size.width * 0.56f, 0f),
                                            Offset(0f, size.height * 0.56f),
                                            Offset(size.width * 0.56f, size.height * 0.56f)
                                        )
                                        val boxSize = Size(size.width * 0.44f, size.height * 0.44f)

                                        boxes.forEachIndexed { i, offset ->
                                            if (isHighlighted && i == 0) {
                                                drawRect(
                                                    color = iconColor,
                                                    topLeft = offset,
                                                    size = boxSize
                                                )
                                            } else {
                                                drawRect(
                                                    color = iconColor,
                                                    topLeft = offset,
                                                    size = boxSize,
                                                    style = Stroke(width = strokeWidth)
                                                )
                                            }
                                        }
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
                                        if (isHighlighted) {
                                            drawPath(
                                                path = path,
                                                color = iconColor.copy(alpha = 0.3f),
                                                style = Fill
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = iconColor,
                                            style = Stroke(
                                                width = if (isHighlighted) 2.2.dp.toPx() else 1.8.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                    3 -> { // Profile Icon
                                        val strokeWidth = if (isHighlighted) 2.2.dp.toPx() else 1.8.dp.toPx()
                                        if (isHighlighted) {
                                            drawCircle(
                                                color = iconColor.copy(alpha = 0.3f),
                                                radius = size.width * 0.25f,
                                                center = Offset(size.width * 0.5f, size.height * 0.3f)
                                            )
                                        }
                                        drawCircle(
                                            color = iconColor,
                                            radius = size.width * 0.25f,
                                            center = Offset(size.width * 0.5f, size.height * 0.3f),
                                            style = Stroke(width = strokeWidth)
                                        )
                                        val bodyPath = Path().apply {
                                            moveTo(0f, size.height)
                                            quadraticTo(size.width * 0.5f, size.height * 0.55f, size.width, size.height)
                                        }
                                        drawPath(
                                            path = bodyPath,
                                            color = iconColor,
                                            style = Stroke(
                                                width = strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = iconColor,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
