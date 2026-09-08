package com.estoyok.app.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PlayBillingManager"
        const val PRODUCT_ID_PREMIUM = "estoyok_premium"
        const val BASE_PLAN_MONTHLY = "monthly-plan"
        const val BASE_PLAN_ANNUAL = "annual-plan"
    }

    private var billingClient: BillingClient? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private var onPurchaseSuccessCallback: ((purchaseToken: String, basePlanId: String) -> Unit)? = null
    private var onPurchaseErrorCallback: ((errorMessage: String) -> Unit)? = null
    private var pendingBasePlanId: String = BASE_PLAN_MONTHLY

    fun initialize() {
        if (billingClient != null && _isReady.value) return

        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()

            startConnection()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BillingClient", e)
        }
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Google Play Billing setup successful")
                    _isReady.value = true
                    querySubscriptionDetails()
                } else {
                    Log.w(TAG, "Google Play Billing setup failed: ${billingResult.debugMessage} (code ${billingResult.responseCode})")
                    _isReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing service disconnected")
                _isReady.value = false
            }
        })
    }

    fun querySubscriptionDetails() {
        val client = billingClient ?: return
        if (!_isReady.value) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, queryResult ->
            val productDetailsList = queryResult.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                val details = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PREMIUM }
                _productDetails.value = details
                Log.i(TAG, "Loaded subscription product: ${details?.productId} with ${details?.subscriptionOfferDetails?.size ?: 0} offers")
            } else {
                Log.w(TAG, "Error querying product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchSubscription(
        activity: Activity,
        billingCycle: String, // "monthly" vs "annual"
        onSuccess: (purchaseToken: String, basePlanId: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        val client = billingClient
        val details = _productDetails.value

        if (client == null || !_isReady.value || details == null) {
            onError("Google Play Billing no está disponible en este momento. Inténtalo de nuevo.")
            return
        }

        this.onPurchaseSuccessCallback = onSuccess
        this.onPurchaseErrorCallback = onError

        val targetBasePlanId = if (billingCycle == "annual") BASE_PLAN_ANNUAL else BASE_PLAN_MONTHLY
        this.pendingBasePlanId = targetBasePlanId

        val offerDetails = details.subscriptionOfferDetails?.firstOrNull {
            it.basePlanId == targetBasePlanId
        } ?: details.subscriptionOfferDetails?.firstOrNull()

        if (offerDetails == null) {
            onError("No se encontró el plan de facturación seleccionado en Google Play.")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerDetails.offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = client.launchBillingFlow(activity, billingFlowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            onError("Error al iniciar el proceso de compra de Google Play (Código $responseCode).")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else {
                    onPurchaseErrorCallback?.invoke("No se recibieron datos de compra.")
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Usuario canceló el flujo de suscripción en Google Play")
                onPurchaseErrorCallback?.invoke("Suscripción cancelada.")
            }
            else -> {
                Log.e(TAG, "Error en compra: ${billingResult.debugMessage}")
                onPurchaseErrorCallback?.invoke(billingResult.debugMessage.ifBlank { "Error en Google Play Billing." })
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.i(TAG, "Suscripción confirmada (acknowledged) ante Google Play exitosamente")
                    } else {
                        Log.w(TAG, "Fallo al confirmar compra ante Google Play: ${ackResult.debugMessage}")
                    }
                }
            }

            onPurchaseSuccessCallback?.invoke(purchase.purchaseToken, pendingBasePlanId)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            onPurchaseErrorCallback?.invoke("Tu suscripción está pendiente de confirmación por Google Play.")
        }
    }
}
