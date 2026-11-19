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
import com.simats.stocksense.core.theme.CardWhite
import com.simats.stocksense.core.theme.InkDark
import com.simats.stocksense.core.theme.MainYellow
import com.simats.stocksense.core.theme.MutedInk
import com.simats.stocksense.core.theme.ShadowColor
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Bold Outline Bottom Navigation Bar with Drag-to-Switch gesture.
 * Features:
 * - White pill with 2.5dp ink border and 4dp hard offset shadow
 * - Selected item = Yellow pill with 2dp ink border
 * - InkDark (#111111) text and icons
 * - Horizontal drag-to-switch with smooth finger tracking and tactile feedback
 * - Inertial fling support and spring snap-to-tab on release
 * - Full accessibility with semantics and minimum 48 dp touch targets
 */
@Composable
fun StockSenseBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF0B1230)
    val inactiveColor = MutedInk

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    // Outer container centered on wide screens with navigation bars insets
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // 4dp Hard Offset Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(ShadowColor, RoundedCornerShape(26.dp))
            )

            // White Pill Container with 2.5dp Ink Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(26.dp))
                    .border(2.5.dp, InkDark, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
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

                    // Motion dynamics
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

                    val pillPressScale by animateFloatAsState(
                        targetValue = if (isSelectedPressed) 0.94f else 1.0f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMedium,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        label = "pillPressScale"
                    )

                    val dragScale by animateFloatAsState(
                        targetValue = if (isDragging) 1.08f else 1.0f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMedium,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        label = "dragScale"
                    )

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

                    val currentPillOffsetDp = with(density) { pillOffsetAnim.value.toDp() }

                    // Yellow Pill Indicator with 2dp Ink Border
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
                            .background(MainYellow, RoundedCornerShape(18.dp))
                            .border(2.dp, InkDark, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
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

                                                val targetTab = when {
                                                    velocityX > flingThreshold -> (selectedTab + 1).coerceAtMost(3)
                                                    velocityX < -flingThreshold -> (selectedTab - 1).coerceAtLeast(0)
                                                    else -> ((currentOffset / tabWidthPx) + 0.5f).toInt().coerceIn(0, 3)
                                                }

                                                dragDeltaX = 0f

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
                                                val tappedIndex = (change.position.x / tabWidthPx).toInt().coerceIn(0, 3)
                                                if (tappedIndex != selectedTab) {
                                                    onTabSelected(tappedIndex)
                                                }
                                            }
                                            break
                                        } else if (!change.pressed) {
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
                                                val newOffset = (initialPillOffset + (change.position.x - startTouchX)).coerceIn(0f, maxOffsetPx)

                                                coroutineScope.launch {
                                                    pillOffsetAnim.snapTo(newOffset)
                                                }

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

                            val tabPressScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.94f else 1.0f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                ),
                                label = "tabPressScale_$index"
                            )

                            val scale by animateFloatAsState(
                                targetValue = if (isHighlighted) 1.10f else 1.0f,
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

                            val iconColor by animateColorAsState(
                                targetValue = if (isHighlighted) activeColor else inactiveColor,
                                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                                label = "iconColor_$index"
                            )

                            val dotAlpha by animateFloatAsState(
                                targetValue = if (isHighlighted) 1f else 0f,
                                animationSpec = tween(durationMillis = 150),
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
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .graphicsLayer {
                                                scaleX = dotScale
                                                scaleY = dotScale
                                                alpha = dotAlpha
                                            }
                                            .clip(CircleShape)
                                            .background(activeColor)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

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
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
