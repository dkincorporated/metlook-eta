package dev.dkong.metlook.eta.objects.metlook

import androidx.annotation.DrawableRes
import dev.dkong.metlook.eta.R

/**
 * Enumeration of the types of stopping patterns
 */
enum class PatternType(@DrawableRes val displayName: Int) {
    AllStops(R.string.pattern_all_stops),
    SkipsOneStop(R.string.pattern_skips_one_stop),
    LimitedStops(R.string.pattern_limited_stops),
    SuperLimitedStops(R.string.pattern_super_limited_stops),
    NotApplicable(R.string.not_applicable)
}