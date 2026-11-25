package com.simats.stocksense.data.model

import com.simats.stocksense.core.avatar.AvatarCode

data class StoreProfile(
    val name: String = "",
    val location: String = "",
    val lastUpdated: String = "",
    val email: String = "",
    val ownerName: String = "",
    val storeType: String = "",
    val storeId: String = "",
    val currency: String = "",
    val role: String = "",
    val plan: String = "",
    val gstin: String? = null,
    val avatar: AvatarCode? = null
)
