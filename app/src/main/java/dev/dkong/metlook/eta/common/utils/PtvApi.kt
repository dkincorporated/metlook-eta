package dev.dkong.metlook.eta.common.utils

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
    private const val BASE_URL = "https://timetableapi.ptv.vic.gov.au"

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

    fun getApiUrl(
        strQuery: String,
        DEV_ID: String = "3000917",
        API_KEY: String = "1062d893-8f26-4642-bfa5-2dfdd3338504"
    ): String? { // TODO: Store the credentials somewhere more secure
        // Automatically concatenates the DEV_ID
        val method = strQuery + "devid=$DEV_ID"
        try {
            val hmac = calculateRFC2104HMAC(method, API_KEY)
            val finalReqUrl = "$BASE_URL$method&Signature=$hmac"
            return finalReqUrl
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    // End encryption methods
}