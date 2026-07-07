package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("provider") val provider: String,
    @SerializedName("plan") val plan: String = "premium"
)

data class CheckoutResponse(
    @SerializedName("checkout_url") val checkoutUrl: String
)
