package com.simats.stocksense.data.repository

import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.OffersApiService
import com.simats.stocksense.data.model.BundleOfferDto
import com.simats.stocksense.data.model.BundleOffersListData
import com.simats.stocksense.data.model.EndBundleOfferRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OffersRepository(
    private val apiService: OffersApiService = NetworkClient.createService()
) {
    suspend fun getOffers(status: String? = null): Result<BundleOffersListData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOffers(status = status)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to load bundle offers."))
                }
            } else if (response.code() == 401) {
                Result.failure(UnauthorizedException("Session expired. Please log in again."))
            } else {
                Result.failure(Exception("Unable to load bundle offers (${response.code()})."))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Network error while loading bundle offers."))
        }
    }

    suspend fun endOffer(offerId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.endOffer(EndBundleOfferRequest(offerId = offerId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to end offer."))
                }
            } else if (response.code() == 401) {
                Result.failure(UnauthorizedException("Session expired. Please log in again."))
            } else {
                Result.failure(Exception("Unable to end offer (${response.code()})."))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Network error while ending offer."))
        }
    }

    suspend fun findActiveOfferPartnerName(productId: Int): String? {
        if (productId <= 0) return null
        val res = getOffers(status = "active")
        val offers: List<BundleOfferDto> = res.getOrNull()?.offers ?: return null
        val match = offers.firstOrNull {
            it.status.equals("active", ignoreCase = true) &&
                (it.product1.productId == productId || it.product2.productId == productId)
        } ?: return null
        return if (match.product1.productId == productId) match.product2.name else match.product1.name
    }
}
