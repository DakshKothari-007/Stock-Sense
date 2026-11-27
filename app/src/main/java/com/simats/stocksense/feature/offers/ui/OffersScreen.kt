package com.simats.stocksense.feature.offers.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.data.model.BundleOfferDto
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.offers.viewmodel.OffersViewModel

@Composable
fun OffersScreen(
    onBackClick: () -> Unit = {},
    viewModel: OffersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var offerPendingEnd by remember { mutableStateOf<BundleOfferDto?>(null) }
    var showEndedSection by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh(force = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        val windowInfo = rememberWindowSizeInfo()

        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            maxWidth = 640.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = windowInfo.horizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                BoldTopBar(
                    title = "Bundle offers",
                    subtitle = "${uiState.activeOffers.size} active",
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    uiState.isLoading && uiState.activeOffers.isEmpty() && uiState.endedOffers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MainBlue)
                        }
                    }

                    uiState.errorMessage != null && uiState.activeOffers.isEmpty() && uiState.endedOffers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = SoftPink,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.errorMessage ?: "Unable to load offers.",
                                        color = boldContentColorFor(SoftPink),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    BoldButton(
                                        text = "Retry",
                                        onClick = { viewModel.refresh(force = true) },
                                        isPrimary = true
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item(key = "offers_note") {
                                BoldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = PaleBlue,
                                    borderColor = InkDark,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = OnPastelIcon,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Offers are a reminder for your counter. Prices in Record Sale do not change automatically.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnPastelInk,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            if (uiState.activeOffers.isEmpty() && uiState.endedOffers.isEmpty()) {
                                item(key = "offers_empty") {
                                    BoldCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = CardWhite,
                                        borderColor = InkDark,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp,
                                        contentPadding = PaddingValues(24.dp)
                                    ) {
                                        Text(
                                            text = "No bundle offers yet. Accept a bundle recommendation to create one.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedInk,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                if (uiState.activeOffers.isEmpty()) {
                                    item(key = "no_active_offers") {
                                        BoldCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            backgroundColor = CardWhite,
                                            borderColor = InkDark,
                                            borderWidth = 2.dp,
                                            shadowOffset = 2.dp,
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            Text(
                                                text = "No active bundle offers right now.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MutedInk,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = uiState.activeOffers,
                                        key = { "active_${it.id}" }
                                    ) { offer ->
                                        BundleOfferCard(
                                            offer = offer,
                                            isEnding = uiState.endingOfferId == offer.id,
                                            onEndOfferClick = { offerPendingEnd = offer }
                                        )
                                    }
                                }

                                if (uiState.endedOffers.isNotEmpty()) {
                                    item(key = "ended_header") {
                                        Surface(
                                            onClick = { showEndedSection = !showEndedSection },
                                            shape = RoundedCornerShape(12.dp),
                                            color = RaisedSurface,
                                            border = BorderStroke(1.5.dp, InkDark),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Ended offers (${uiState.endedOffers.size})",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkDark
                                                )
                                                Icon(
                                                    imageVector = if (showEndedSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = InkDark
                                                )
                                            }
                                        }
                                    }

                                    if (showEndedSection) {
                                        items(
                                            items = uiState.endedOffers,
                                            key = { "ended_${it.id}" }
                                        ) { offer ->
                                            BundleOfferCard(
                                                offer = offer,
                                                isEnding = false,
                                                onEndOfferClick = null
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

    val pending = offerPendingEnd
    if (pending != null) {
        BoldDialog(onDismissRequest = { offerPendingEnd = null }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "End bundle offer?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    text = "End the offer for ${pending.product1.name} + ${pending.product2.name}? This will move it to Ended offers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BoldButton(
                        text = "Cancel",
                        onClick = { offerPendingEnd = null },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                    BoldButton(
                        text = "End offer",
                        onClick = {
                            val idToEnd = pending.id
                            offerPendingEnd = null
                            viewModel.endOffer(
                                offerId = idToEnd,
                                onSuccess = {
                                    Toast.makeText(context, "Bundle offer ended.", Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BundleOfferCard(
    offer: BundleOfferDto,
    isEnding: Boolean,
    onEndOfferClick: (() -> Unit)?
) {
    val isActive = offer.status.equals("active", ignoreCase = true)
    val offerPriceStr = NumberFormatUtil.formatIndianRupees(offer.offerPrice)
    val savingsStr = NumberFormatUtil.formatIndianRupees(offer.savings)
    val dateLabel = remember(offer.createdAt) {
        val raw = offer.createdAt.orEmpty().trim()
        if (raw.isEmpty()) "" else {
            try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                val out = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
                parser.parse(raw)?.let { out.format(it) } ?: raw.take(10)
            } catch (_: Throwable) {
                raw.take(10)
            }
        }
    }

    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isActive) CardWhite else RaisedSurface,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = if (isActive) 3.dp else 2.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    ProductAvatar(
                        name = offer.product1.name,
                        category = offer.product1.category,
                        iconKey = offer.product1.iconKey,
                        size = 40.dp
                    )
                    ProductAvatar(
                        name = offer.product2.name,
                        category = offer.product2.category,
                        iconKey = offer.product2.iconKey,
                        size = 40.dp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${offer.product1.name} + ${offer.product2.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Offer price $offerPriceStr (save $savingsStr)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MainBlue else MutedInk,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dateLabel.isNotBlank()) "Created $dateLabel" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp
                )

                if (isActive && onEndOfferClick != null) {
                    BoldButton(
                        text = if (isEnding) "Ending..." else "End offer",
                        onClick = onEndOfferClick,
                        enabled = !isEnding,
                        isPrimary = false,
                        borderWidth = 1.5.dp,
                        shadowOffset = 2.dp,
                        modifier = Modifier
                            .wrapContentWidth()
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftLavender,
                        border = BorderStroke(1.dp, InkDark)
                    ) {
                        Text(
                            text = "ENDED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnPastelInk,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
