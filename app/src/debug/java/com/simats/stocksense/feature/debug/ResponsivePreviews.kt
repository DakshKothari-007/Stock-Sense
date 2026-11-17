package com.simats.stocksense.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.data.model.DashboardSummaryData
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.feature.ai.ui.AiChatScreen
import com.simats.stocksense.feature.analytics.ui.RecordSaleScreen
import com.simats.stocksense.feature.analytics.ui.SalesAnalyticsScreen
import com.simats.stocksense.feature.auth.ui.DeleteAccountScreen
import com.simats.stocksense.feature.auth.ui.ForgotPasswordScreen
import com.simats.stocksense.feature.auth.ui.LoginScreen
import com.simats.stocksense.feature.auth.ui.OnboardingScreen1
import com.simats.stocksense.feature.auth.ui.SignUpScreen
import com.simats.stocksense.feature.auth.ui.SplashScreen
import com.simats.stocksense.feature.dashboard.ui.DashboardHomeContent
import com.simats.stocksense.feature.dashboard.ui.InsightsContent
import com.simats.stocksense.feature.dashboard.ui.NotificationsScreen
import com.simats.stocksense.feature.dashboard.ui.ProfileContent
import com.simats.stocksense.feature.dashboard.ui.SettingsScreen
import com.simats.stocksense.feature.inventory.ui.AddProductScreenContent
import com.simats.stocksense.feature.inventory.ui.EditProductScreenContent
import com.simats.stocksense.feature.inventory.ui.ImportInventoryPreviewScreen
import com.simats.stocksense.feature.inventory.ui.ImportInventoryUploadScreen
import com.simats.stocksense.feature.inventory.ui.InventoryContent
import com.simats.stocksense.feature.inventory.ui.ProductDetailScreenContent
import com.simats.stocksense.feature.inventory.viewmodel.ProductDetailUiState
import com.simats.stocksense.feature.inventory.viewmodel.ProductFormUiState
import com.simats.stocksense.feature.reorder.ui.OrderDetailScreen
import com.simats.stocksense.feature.reorder.ui.PurchaseOrdersScreen
import com.simats.stocksense.feature.reorder.ui.ReorderRecommendationScreen

/**
 * Multipreview annotation covering all required test viewports and font scales:
 * - 320x568 (Small legacy phone / compact 320dp width)
 * - 360x640 (Standard compact Android phone)
 * - 360x800 (Modern standard phone)
 * - 411x891 (Pixel / large modern aspect)
 * - 600x960 (Tablet / Foldable expanded width)
 * - 800x360 (Landscape orientation)
 * - Font scale 0.85x (Small system font)
 * - Font scale 1.3x (Large accessibility font)
 * - Font scale 2.0x (Maximum accessibility font)
 *
 * Located strictly in app/src/debug/java to ensure zero release bundle overhead.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Preview(name = "1. Small Phone (320x568)", widthDp = 320, heightDp = 568, showBackground = true)
@Preview(name = "2. Small Standard (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Preview(name = "3. Modern Standard (360x800)", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "4. Pixel / Tall (411x891)", widthDp = 411, heightDp = 891, showBackground = true)
@Preview(name = "5. Tablet (600x960)", widthDp = 600, heightDp = 960, showBackground = true)
@Preview(name = "6. Landscape (800x360)", widthDp = 800, heightDp = 360, showBackground = true)
@Preview(name = "7. Small Font 0.85x (360x800)", widthDp = 360, heightDp = 800, fontScale = 0.85f, showBackground = true)
@Preview(name = "8. Large Font 1.3x (360x800)", widthDp = 360, heightDp = 800, fontScale = 1.3f, showBackground = true)
@Preview(name = "9. Max Font 2.0x (360x800)", widthDp = 360, heightDp = 800, fontScale = 2.0f, showBackground = true)
annotation class ResponsiveDevicesPreview

@ResponsiveDevicesPreview
@Composable
fun LoginScreenResponsivePreview() {
    StockSenseTheme {
        LoginScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun SignUpScreenResponsivePreview() {
    StockSenseTheme {
        SignUpScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun DashboardHomeScreenResponsivePreview() {
    StockSenseTheme {
        DashboardHomeContent(
            data = DashboardSummaryData(),
            unreadCount = 2,
            lastUpdatedText = "30 Oct, 1:30 pm"
        )
    }
}

@ResponsiveDevicesPreview
@Composable
fun InventoryScreenResponsivePreview() {
    StockSenseTheme {
        InventoryContent(
            products = listOf(
                InventoryProduct(
                    id = "1",
                    name = "Amul Taaza Homogenised Toned Milk",
                    sku = "DAI-001",
                    price = "₹30",
                    status = "Low stock · Minimum 24",
                    statusColorHex = 0xFFFF9F5A,
                    units = "8",
                    iconType = 0
                ),
                InventoryProduct(
                    id = "2",
                    name = "Aashirvaad Superior MP Sharbati Atta 5kg",
                    sku = "ATT-002",
                    price = "₹280",
                    status = "Healthy stock",
                    statusColorHex = 0xFFC9B8FF,
                    units = "45",
                    iconType = 0
                )
            )
        )
    }
}

@ResponsiveDevicesPreview
@Composable
fun AddProductScreenResponsivePreview() {
    StockSenseTheme {
        AddProductScreenContent(
            uiState = ProductFormUiState(
                name = "Tata Salt Vacuum Evaporated 1kg",
                sku = "GRO-001",
                sellingPrice = "28.00",
                purchaseCost = "22.00",
                stock = "50",
                minStock = "15"
            ),
            onBackClick = {},
            onNameChange = {},
            onSkuChange = {},
            onCategoryChange = {},
            onSellingPriceChange = {},
            onPurchaseCostChange = {},
            onStockChange = {},
            onMinStockChange = {},
            onDismissBanner = {},
            onSaveClick = {}
        )
    }
}

@ResponsiveDevicesPreview
@Composable
fun EditProductScreenResponsivePreview() {
    StockSenseTheme {
        EditProductScreenContent(
            uiState = ProductFormUiState(
                name = "Tata Salt Vacuum Evaporated 1kg",
                sku = "GRO-001",
                sellingPrice = "28.00",
                purchaseCost = "22.00",
                stock = "50",
                minStock = "15"
            ),
            onBackClick = {},
            onNameChange = {},
            onSkuChange = {},
            onCategoryChange = {},
            onSellingPriceChange = {},
            onPurchaseCostChange = {},
            onStockChange = {},
            onMinStockChange = {},
            onDismissBanner = {},
            onDismissDeleteConflict = {},
            onSaveClick = {},
            onDeleteClick = {}
        )
    }
}

@ResponsiveDevicesPreview
@Composable
fun ProductDetailScreenResponsivePreview() {
    StockSenseTheme {
        ProductDetailScreenContent(
            uiState = ProductDetailUiState(
                lastUpdatedDateText = "Mon, 9 Nov 2026"
            )
        )
    }
}

@ResponsiveDevicesPreview
@Composable
fun RecordSaleScreenResponsivePreview() {
    StockSenseTheme {
        RecordSaleScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun SalesAnalyticsScreenResponsivePreview() {
    StockSenseTheme {
        SalesAnalyticsScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun PurchaseOrdersScreenResponsivePreview() {
    StockSenseTheme {
        PurchaseOrdersScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun OrderDetailScreenResponsivePreview() {
    StockSenseTheme {
        OrderDetailScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun ReorderRecommendationScreenResponsivePreview() {
    StockSenseTheme {
        ReorderRecommendationScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun InsightsScreenResponsivePreview() {
    StockSenseTheme {
        InsightsContent()
    }
}

@ResponsiveDevicesPreview
@Composable
fun AiChatScreenResponsivePreview() {
    StockSenseTheme {
        AiChatScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun NotificationsScreenResponsivePreview() {
    StockSenseTheme {
        NotificationsScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun SettingsScreenResponsivePreview() {
    StockSenseTheme {
        SettingsScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun ProfileScreenResponsivePreview() {
    StockSenseTheme {
        ProfileContent()
    }
}

@ResponsiveDevicesPreview
@Composable
fun DeleteAccountScreenResponsivePreview() {
    StockSenseTheme {
        DeleteAccountScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun ForgotPasswordScreenResponsivePreview() {
    StockSenseTheme {
        ForgotPasswordScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun ImportInventoryUploadScreenResponsivePreview() {
    StockSenseTheme {
        ImportInventoryUploadScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun ImportInventoryPreviewScreenResponsivePreview() {
    StockSenseTheme {
        ImportInventoryPreviewScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun SplashScreenResponsivePreview() {
    StockSenseTheme {
        SplashScreen()
    }
}

@ResponsiveDevicesPreview
@Composable
fun OnboardingScreen1ResponsivePreview() {
    StockSenseTheme {
        OnboardingScreen1()
    }
}
