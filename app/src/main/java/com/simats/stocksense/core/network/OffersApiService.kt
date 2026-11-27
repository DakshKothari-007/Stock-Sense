package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.BundleOffersListResponse
import com.simats.stocksense.data.model.EndBundleOfferRequest
import com.simats.stocksense.data.model.EndBundleOfferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OffersApiService {

    @GET("offers/list.php")
    suspend fun getOffers(
        @Query("status") status: String? = null
    ): Response<BundleOffersListResponse>

    @POST("offers/end.php")
    suspend fun endOffer(
        @Body request: EndBundleOfferRequest
    ): Response<EndBundleOfferResponse>
}
