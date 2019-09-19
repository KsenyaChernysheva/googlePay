package com.example.xenya.googlepay

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var googlePaymentsClient: PaymentsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googlePaymentsClient = GooglePaymentUtils.createGoogleApiClientForPay(this)

        btn_google_pay.setOnClickListener {
            val request = GooglePaymentUtils.createPaymentDataRequest("69.00")
            AutoResolveHelper.resolveTask<PaymentData>(
                googlePaymentsClient.loadPaymentData(request),
                this,
                REQUEST_CODE
            )
        }

        GooglePaymentUtils.checkIsReadyGooglePay(googlePaymentsClient) {
            btn_google_pay.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data == null)
                        return

                    val paymentData = PaymentData.getFromIntent(data)
                    val tokenJSON = paymentData?.paymentMethodToken?.token
                    Token.fromString(tokenJSON)?.let { token ->
                        Toast.makeText(this, "You rich bitch!!!\nCongratu;ations\nYour token is ${token.id}", Toast.LENGTH_LONG).show()
                    } ?: run {
                        Log.e("GOOGLE PAY", "Failed to parse")
                    }

                }
                Activity.RESULT_CANCELED -> {
                    // Пользователь нажал назад,
                    // когда был показан диалог google pay
                    // если показывали загрузку или что-то еще,
                    // можете отменить здесь
                    Toast.makeText(this, "Bomj ebuchiy oplatit ne smog!!!", Toast.LENGTH_LONG).show()
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    if (data == null)
                        return

                    // Гугл сам покажет диалог ошибки.
                    // Можете вывести логи и спрятать загрузку,
                    // если показывали
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Log.e("GOOGLE PAY", "Load payment data has failed with status: $status")
                }
                else -> { }
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 797
    }
}
