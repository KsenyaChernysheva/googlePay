package com.example.xenya.googlepay

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*

object GooglePaymentUtils {

    private const val CURRENCY_CODE = "RUB"

    private val SUPPORTED_NETWORKS = arrayListOf(WalletConstants.CARD_NETWORK_OTHER,
        WalletConstants.CARD_NETWORK_VISA,
        WalletConstants.CARD_NETWORK_MASTERCARD)



    fun createGoogleApiClientForPay(context: Context): PaymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .setTheme(WalletConstants.THEME_LIGHT)
                .build()
        )

    fun checkIsReadyGooglePay(
        paymentsClient: PaymentsClient,
        callback: (res: Boolean) -> Unit
    ) {
        val isReadyRequest = IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build()
        val task = paymentsClient.isReadyToPay(isReadyRequest)
        task.addOnCompleteListener {
            try {
                if (it.getResult(ApiException::class.java) == true)
                // можем показать кнопку оплаты, все хорошо
                    callback.invoke(true)
                else
                // должны спрятать кнопку оплаты
                    callback.invoke(false)
            } catch (e: ApiException) {
                e.printStackTrace()
                callback.invoke(false)
            }
        }
    }

    fun createPaymentDataRequest(price: String): PaymentDataRequest {
        val transaction = createTransaction(price)
        val request = generatePaymentRequest(transaction)
        return request
    }

    fun createTransaction(price: String): TransactionInfo =
        TransactionInfo.newBuilder()
            .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
            .setTotalPrice(price)
            .setCurrencyCode(CURRENCY_CODE)
            .build()

    private fun generatePaymentRequest(transactionInfo: TransactionInfo): PaymentDataRequest {
        val tokenParams = PaymentMethodTokenizationParameters
            .newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .addParameter("gateway", "stripe")
            .addParameter("stripe:publishableKey", "pk_test_5zDGfNb4NW148U96lYV6qde800K3JWSRdA")
            .addParameter("stripe:version", "2019-09-09")
            .build()

        return PaymentDataRequest.newBuilder()
            .setPhoneNumberRequired(false)
            .setEmailRequired(false)
            .setShippingAddressRequired(false)
            .setTransactionInfo(transactionInfo)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(SUPPORTED_NETWORKS)
                    .setAllowPrepaidCards(true)
                    .setBillingAddressRequired(false)
                    .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                    .build()
            )
            .setPaymentMethodTokenizationParameters(tokenParams)
            .setUiRequired(false)
            .build()
    }

}
