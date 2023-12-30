package dev.dkong.metlook.eta.common.datastore.settings

import androidx.datastore.preferences.core.stringPreferencesKey

object RecentsSettingsDataStore : TransformedSettingsDataStore<Pair<Int, Int>, String>(
    stringPreferencesKey("recents_limits")) {
    /**
     * Convert the two values into a string-based value
     * @param data [Pair] of stops and services limit
     */
    override fun serialise(data: Pair<Int, Int>): String = "${data.first},${data.second}"

    /**
     * Convert the comma-delimited string into a [Pair]
     */
    override fun deserialise(data: String?): Pair<Int, Int> = data?.split(",").let {
        Pair(it?.get(0)?.toInt() ?: 0, it?.get(1)?.toInt() ?: 0)
    }
}