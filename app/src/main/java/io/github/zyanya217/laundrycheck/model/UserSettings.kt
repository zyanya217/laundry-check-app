package io.github.zyanya217.laundrycheck.model

enum class ThemeChoice { AUTO, LIGHT, DARK }

const val DEFAULT_COUNTY = "臺北市"
const val DEFAULT_DISTRICT = "南港區"

/** Everything the person can change in the settings sheet, except the language (the system keeps that). */
data class UserSettings(
    val county: String = DEFAULT_COUNTY,
    val district: String = DEFAULT_DISTRICT,
    val startHour: Int = 21,
    val collectHour: Int = 8,
    /** The balcony has a roof, so rain probability counts for less. */
    val covered: Boolean = false,
    /** Remind in the evening when tonight is good for drying. */
    val notifyEnabled: Boolean = false,
    /** Taipei hour (0-23) from which the reminder may be sent. */
    val notifyHour: Int = 20,
    val theme: ThemeChoice = ThemeChoice.AUTO,
)
