package dev.dkong.metlook.eta.common.utils

import android.net.Uri
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.util.Formatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Methods to access the PTV API
 */
object PtvApi {
    // Encryption methods
    private const val HMAC_SHA1_ALGORITHM = "HmacSHA1"
    private const val BASE_URL = "timetableapi.ptv.vic.gov.au"
    private const val DEV_ID: String = "DEV_ID"
    private const val API_KEY: String = "API_KEY"

    /**
     * Convert bytes to hex in string
     */
    private fun toHexString(bytes: ByteArray): String {
        val formatter = Formatter()
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return formatter.toString()
    }

    /**
     * Run the signature calculation
     */
    @Throws(
        SignatureException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class
    )
    private fun calculateRFC2104HMAC(data: String, key: String): String {
        val signingKey = SecretKeySpec(key.toByteArray(), HMAC_SHA1_ALGORITHM)
        val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
        mac.init(signingKey)
        return toHexString(mac.doFinal(data.toByteArray()))
    }

    /**
     * Get the full API URL from a base method
     *
     * Protocol and base domain are **not** required.
     * @param query the API query method in a [Uri.Builder]
     */
    fun getApiUrl(
        query: Uri.Builder
    ): String? {
        val method = query
            .appendQueryParameter("devid", DEV_ID)
        try {
            val hmac = calculateRFC2104HMAC(method.build().toString(), API_KEY)
            return method
                .scheme("https")
                .authority(BASE_URL)
                .appendQueryParameter("Signature", hmac)
                .build().toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    // End encryption methods
}