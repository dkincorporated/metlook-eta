package dev.dkong.metlook.eta.objects.metlook

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.dkong.metlook.eta.R

/**
 * Enumeration of the types of stopping patterns
 */
enum class PatternType(@StringRes val displayName: Int, val patternClass: PatternClass) {
    AllStops(R.string.pattern_all_stops, PatternClass.Local),
    SkipsOneStop(R.string.pattern_skips_one_stop, PatternClass.Local),
    LimitedStops(R.string.pattern_limited_stops, PatternClass.LimitedStops),
    SuperLimitedStops(R.string.pattern_super_limited_stops, PatternClass.LimitedStops),
    NotApplicable(R.string.not_applicable, PatternClass.Local);

    /**
     * Classes of pattern types
     */
    enum class PatternClass(@StringRes val displayName: Int) {
        Local(R.string.pattern_class_local),
        LimitedStops(R.string.pattern_class_limited_stops)
    }
}