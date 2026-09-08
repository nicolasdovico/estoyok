package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("provider") val provider: String,
    @SerializedName("plan") val plan: String = "premium"
)

data class CheckoutResponse(
    @SerializedName("checkout_url") val checkoutUrl: String
)

data class StartTrialResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("checkout_url") val checkoutUrl: String? = null
)

data class VerifyGooglePlayRequest(
    @SerializedName("purchase_token") val purchaseToken: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("base_plan_id") val basePlanId: String? = null
)

data class VerifyGooglePlayResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: com.estoyok.app.features.auth.data.model.UserDto? = null
)
