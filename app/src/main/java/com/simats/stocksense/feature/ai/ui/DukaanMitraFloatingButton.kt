package com.simats.stocksense.feature.ai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simats.stocksense.R
import com.simats.stocksense.core.theme.CardWhite
import com.simats.stocksense.core.theme.InkDark
import com.simats.stocksense.core.theme.MainBlue

@Composable
fun DukaanMitraFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .scale(pulseScale)
    ) {
        // 4dp Hard shadow
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(x = 4.dp, y = 4.dp)
                .background(InkDark, CircleShape)
        )
        // Outer button with blue ring + 2.5dp ink border
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MainBlue, CircleShape)
                .border(2.5.dp, InkDark, CircleShape)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(CardWhite, CircleShape)
                    .border(1.5.dp, InkDark, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dukaan_mitra),
                    contentDescription = "Open Dukaan Mitra AI",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

