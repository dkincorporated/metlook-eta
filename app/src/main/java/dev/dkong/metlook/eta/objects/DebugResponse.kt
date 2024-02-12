package dev.dkong.metlook.eta.objects

/**
 * A carrier for a processed response object and the raw response
 * @param data the processed data object
 * @param response the raw string web response
 * @param T type of processed object
 */
data class DebugResponse<T>(
    val data: T,
    val response: String
)
