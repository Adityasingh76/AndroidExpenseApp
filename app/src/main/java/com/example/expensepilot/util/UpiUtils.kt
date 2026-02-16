package com.example.expensepilot.util

import android.net.Uri

data class UpiDetails(
    val upiId: String?,
    val payeeName: String?
)

object UpiUtils {
    fun parseUpiQr(content: String): UpiDetails? {
        return try {
            val uri = Uri.parse(content)
            if (uri.scheme != "upi") return null
            UpiDetails(
                upiId = uri.getQueryParameter("pa"),
                payeeName = uri.getQueryParameter("pn")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun buildUpiUri(
        upiId: String,
        payeeName: String,
        amount: Double,
        note: String,
        category: String
    ): Uri {
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("am", "%.2f".format(amount))
            .appendQueryParameter("tn", "$category: $note")
            .appendQueryParameter("cu", "INR")
            .build()
    }
}
