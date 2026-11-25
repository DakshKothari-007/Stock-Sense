package com.simats.stocksense.core.avatar

import com.simats.stocksense.BuildConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Deterministic URL builder for DiceBear HTTP API SVG requests.
 * Sends only the style, anonymous seed, and whitelisted friendly customization options,
 * and always enforces facialHairProbability=0 across all styles (plus maskProbability=0
 * on open-peeps) so DiceBear never renders beards or masks by chance.
 */
object AvatarUrlBuilder {

    fun buildUrl(
        avatar: AvatarCode?,
        baseUrl: String = BuildConfig.DICEBEAR_BASE_URL
    ): String? {
        val valid = AvatarCatalog.parseAvatarCode(avatar) ?: return null
        val trimmedBase = baseUrl.trim().trimEnd('/')
        if (trimmedBase.isEmpty()) return null

        val sb = StringBuilder()
        sb.append(trimmedBase)
            .append('/')
            .append(valid.style)
            .append("/svg?seed=")
            .append(encodeParam(valid.seed))

        // Sort keys so identical avatar configurations always produce identical disk cache keys.
        val sortedOptions = valid.options.toSortedMap()
        // Never allow DiceBear's default random facial hair chance on any supported style.
        sortedOptions["facialHairProbability"] = "0"
        if (valid.style == AvatarStyle.OPEN_PEEPS.id) {
            // Suppress DiceBear's 5% default mask chance in Open Peeps without storing a mask option.
            sortedOptions["maskProbability"] = "0"
        }

        for ((key, value) in sortedOptions) {
            sb.append('&')
                .append(encodeParam(key))
                .append('=')
                .append(encodeParam(value))
        }
        return sb.toString()
    }

    private fun encodeParam(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
}
