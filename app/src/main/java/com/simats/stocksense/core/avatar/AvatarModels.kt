package com.simats.stocksense.core.avatar

import com.google.gson.annotations.SerializedName

/**
 * Supported DiceBear avatar styles and their license attribution metadata.
 */
enum class AvatarStyle(
    val id: String,
    val displayName: String,
    val creditAuthor: String,
    val license: String
) {
    OPEN_PEEPS(
        id = "open-peeps",
        displayName = "Open Peeps",
        creditAuthor = "Pablo Stanley",
        license = "CC0"
    ),
    MICAH(
        id = "micah",
        displayName = "Micah",
        creditAuthor = "Micah Lanier",
        license = "CC BY 4.0"
    ),
    PERSONAS(
        id = "personas",
        displayName = "Personas",
        creditAuthor = "Draftbit",
        license = "CC BY 4.0"
    );

    companion object {
        fun fromId(id: String?): AvatarStyle? =
            entries.firstOrNull { it.id == id }
    }
}

/**
 * Portable avatar configuration persisted in DB and session state:
 * { style, seed, options }.
 * Never contains email, name, phone, or other personal identifiers.
 */
data class AvatarCode(
    @SerializedName("style") val style: String = "",
    @SerializedName("seed") val seed: String = "",
    @SerializedName("options") val options: Map<String, String> = emptyMap()
)

/**
 * UI customization row types rendered in Step 2 ("Style it").
 */
sealed class AvatarOptionRow {
    abstract val title: String

    data class ColorSwatches(
        override val title: String,
        val optionKey: String,
        val colors: List<String>
    ) : AvatarOptionRow()

    data class VariantChips(
        override val title: String,
        val optionKey: String,
        val values: List<String>
    ) : AvatarOptionRow()

    data class OptionalVariantChips(
        override val title: String,
        val variantKey: String,
        val probabilityKey: String,
        val values: List<String>
    ) : AvatarOptionRow()
}
