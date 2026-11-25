package com.simats.stocksense.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.simats.stocksense.core.avatar.AvatarCatalog
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.avatar.AvatarUrlBuilder
import com.simats.stocksense.core.theme.CardWhite
import com.simats.stocksense.core.theme.InkDark
import com.simats.stocksense.core.theme.MainYellow

/**
 * Bold Outline user avatar circle.
 *
 * Displays the Coil-cached DiceBear SVG when [avatarCode] is valid and loaded, and gracefully falls
 * back to the owner's letter badge when unset, while loading, on network/decode error, or offline.
 */
@Composable
fun UserAvatarBadge(
    avatarCode: AvatarCode?,
    displayName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    borderWidth: Dp = 2.dp,
    fontSize: TextUnit = 18.sp,
    showEditBadge: Boolean = false,
    contentDescription: String = "User avatar",
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val initials = remember(displayName) { AvatarCatalog.extractInitials(displayName) }
    val imageUrl = remember(avatarCode) { AvatarUrlBuilder.buildUrl(avatarCode) }

    var isImageLoaded by remember(imageUrl) { mutableStateOf(false) }

    val clickModifier = if (onClick != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        val circleBackground: Color = if (imageUrl != null && isImageLoaded) {
            CardWhite
        } else {
            MainYellow
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(circleBackground)
                .border(borderWidth, InkDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!isImageLoaded || imageUrl == null) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B1230),
                    fontSize = fontSize
                )
            }

            if (imageUrl != null && !isInspection) {
                val request = remember(imageUrl) {
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    onLoading = { isImageLoaded = false },
                    onError = { isImageLoaded = false },
                    onSuccess = { isImageLoaded = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isImageLoaded) 1f else 0f)
                )
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .border(1.5.dp, InkDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Customize avatar",
                    tint = InkDark,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
