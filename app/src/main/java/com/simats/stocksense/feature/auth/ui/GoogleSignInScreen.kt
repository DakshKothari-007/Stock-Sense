package com.simats.stocksense.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*

@Composable
fun GoogleSignInScreen(
    onAccountSelected: (String) -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Google 'G' Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .border(2.dp, InkDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Choose an account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "to continue to StockSense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Demo Account Item 1
                AccountItem(
                    initial = "A",
                    name = "Ananya Rao",
                    email = "ananya@greenbasket.in",
                    onClick = { onAccountSelected("ananya@greenbasket.in") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Demo Account Item 2
                AccountItem(
                    initial = "G",
                    name = "Green Basket Retail",
                    email = "admin@greenbasket.in",
                    onClick = { onAccountSelected("admin@greenbasket.in") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Use another account
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite)
                        .border(2.dp, InkDark, RoundedCornerShape(12.dp))
                        .clickable { onAccountSelected("other@domain.com") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftLavender)
                            .border(1.5.dp, InkDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            drawLine(
                                color = InkDark,
                                start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = InkDark,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.5f),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.5f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Use another account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoldButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To continue, Google will share your name, email address, and language preference with StockSense.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun AccountItem(
    initial: String,
    name: String,
    email: String,
    onClick: () -> Unit
) {
    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftLightBlue)
                    .border(1.5.dp, InkDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = InkDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoogleSignInScreenPreview() {
    StockSenseTheme {
        GoogleSignInScreen()
    }
}
