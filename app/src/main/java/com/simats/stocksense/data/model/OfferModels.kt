package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

data class BundleOfferProductDto(
    @SerializedName("product_id") val productId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("icon_key") val iconKey: String? = null
)

data class BundleOfferDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("product_1") val product1: BundleOfferProductDto = BundleOfferProductDto(),
    @SerializedName("product_2") val product2: BundleOfferProductDto = BundleOfferProductDto(),
    @SerializedName("offer_price") val offerPrice: Double = 0.0,
    @SerializedName("combined_price") val combinedPrice: Double = 0.0,
    @SerializedName("savings") val savings: Double = 0.0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("ended_at") val endedAt: String? = null
)

data class BundleOffersListData(
    @SerializedName("active_count") val activeCount: Int = 0,
    @SerializedName("ended_count") val endedCount: Int = 0,
    @SerializedName("offers") val offers: List<BundleOfferDto> = emptyList()
)

data class BundleOffersListResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BundleOffersListData? = null
)

data class EndBundleOfferRequest(
    @SerializedName("offer_id") val offerId: Int
)

data class EndBundleOfferData(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("status") val status: String = "ended",
    @SerializedName("ended_at") val endedAt: String? = null
)

data class EndBundleOfferResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: EndBundleOfferData? = null
)
