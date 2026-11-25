package com.simats.stocksense.core.avatar

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.security.SecureRandom
import java.util.Locale
import java.util.Random

/**
 * Hardcoded friendly-only DiceBear 10.x option whitelist (zero facial hair), validation,
 * parsing, base grid presets, and legacy fallback mapping.
 * Matches backend includes/AvatarCatalog.php strictly so client and server enforce identical rules.
 */
object AvatarCatalog {

    const val MAX_OPTIONS_JSON_BYTES = 1024

    private val SEED_REGEX = Regex("^[A-Za-z0-9_-]{1,64}$")
    private val HEX_6_REGEX = Regex("^[0-9a-f]{6}$")
    private val PROB_REGEX = Regex("^(0|[1-9][0-9]{0,2})$")
    private val ALNUM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
    private val secureRandom = SecureRandom()
    private val gson: Gson = GsonBuilder().create()

    val BACKGROUND_COLORS: List<String> = listOf(
        "fff4dc", "dce6ff", "9db8ff", "ffc93c", "ffd6a5", "ffb8d0",
        "c9b8ff", "b6e3f4", "c0aede", "d1d4f9", "ffd5dc", "ffdfbf",
        "ffffff", "transparent"
    )

    // Fixed opaque seeds for the 12-item Step 1 grid (3 per row x 4 rows) per style.
    // Never derived from email, name, or phone.
    val BASE_SEEDS_BY_STYLE: Map<AvatarStyle, List<String>> = mapOf(
        AvatarStyle.OPEN_PEEPS to listOf(
            "op_m4x9k2a1", "op_p7r3w8b2", "op_c5t1n6d3", "op_h8v4z9e4",
            "op_k2j7y3f5", "op_q6l5m1g6", "op_w9n8p4h7", "op_z3b2x7j8",
            "op_f4d6r2k9", "op_g8s1v5m0", "op_t2c9h4n1", "op_x5y3q8p2"
        ),
        AvatarStyle.MICAH to listOf(
            "mc_r8k3v2n1", "mc_w4t9p6m2", "mc_d7x2l5q3", "mc_b1z8c4f4",
            "mc_h6y5g9j5", "mc_n3m1w7k6", "mc_s9p4r2t7", "mc_j2f6d8v8",
            "mc_l5c3x1y9", "mc_q7h9b4n0", "mc_v4g2k8p1", "mc_a8w6t3z2"
        ),
        AvatarStyle.PERSONAS to listOf(
            "ps_t6n2r9w1", "ps_y3v8k4m2", "ps_p5h1z7c3", "ps_x9d4b2q4",
            "ps_g1w7j6l5", "ps_c8f3n5v6", "ps_k4m9t2p7", "ps_b2q6x8h8",
            "ps_z7l1y4r9", "ps_n5p8d3w0", "ps_m3r6v9k1", "ps_f9c2g7t2"
        )
    )

    private sealed class Rule {
        data class EnumValues(val values: Set<String>) : Rule()
        data class ColorPalette(val values: Set<String>) : Rule()
        data object Probability : Rule()
    }

    // ---- open-peeps friendly-only whitelist ----
    private val openPeepsSkinColors = listOf("ffdbb4", "edb98a", "d08b5b", "ae5d29", "694d3d")
    private val openPeepsHeadVariants = listOf(
        "afro", "bangs", "bangs2", "bantuKnots", "bear", "bun", "bun2", "buns",
        "cornrows", "cornrows2", "dreads1", "dreads2", "flatTop", "flatTopLong",
        "grayBun", "grayMedium", "grayShort", "hatBeanie", "hatHip", "hijab",
        "long", "longAfro", "longBangs", "longCurly", "medium1", "medium2",
        "medium3", "mediumBangs", "mediumBangs2", "mediumBangs3", "mediumStraight",
        "mohawk", "mohawk2", "noHair1", "noHair2", "noHair3", "pomp",
        "shaved1", "shaved2", "shaved3", "short1", "short2", "short3",
        "short4", "short5", "turban", "twists", "twists2"
    )
    private val openPeepsHeadContrastColors = listOf(
        "2c1b18", "e8e1e1", "ecdcbf", "d6b370", "f59797",
        "b58143", "a55728", "724133", "4a312c", "c93305"
    )
    val openPeepsExpressions: List<String> = listOf(
        "smile", "smileBig", "smileLOL", "smileTeethGap", "calm", "cute",
        "cheeky", "lovingGrin1", "lovingGrin2", "eatingHappy", "driven",
        "explaining", "awe"
    )
    private val openPeepsClothingColors = listOf(
        "8fa7df", "78e185", "ffcf77", "e279c7", "e78276", "9ddadb", "fdea6b"
    )
    val openPeepsAccessories: List<String> = listOf(
        "glasses", "glasses2", "glasses3", "glasses4", "glasses5",
        "sunglasses", "sunglasses2"
    )

    // ---- micah friendly-only whitelist ----
    private val micahSkinColors = listOf("f9c9b6", "ac6651", "77311d")
    private val micahAccentColors = listOf(
        "f9c9b6", "d2eff3", "000000", "e0ddff", "f4d150", "ac6651",
        "9287ff", "ffeba4", "fc909f", "ffedef", "6bd9e9", "77311d", "ffffff"
    )
    private val micahHairVariants = listOf(
        "dannyPhantom", "dougFunny", "fonze", "full", "mrClean", "mrT", "pixie", "turban"
    )
    val micahEyesVariants: List<String> = listOf(
        "eyes", "round", "smiling", "eyesShadow", "smilingShadow"
    )
    val micahEyebrowsVariants: List<String> = listOf("up", "eyelashesUp")
    val micahMouthVariants: List<String> = listOf("laughing", "smile", "smirk", "pucker")
    private val micahNoseVariants = listOf("curve", "pointed", "tound")
    private val micahEarsVariants = listOf("attached", "detached")
    private val micahClothes = listOf("collared", "crew", "open")
    private val micahGlasses = listOf("round", "square")
    private val micahEarrings = listOf("hoop", "stud")
    private val micahEyeShadowColors = listOf("d2eff3", "e0ddff", "ffeba4", "ffedef", "ffffff")

    // ---- personas friendly-only whitelist ----
    private val personasSkinColors = listOf(
        "eeb4a4", "e7a391", "e5a07e", "d78774", "b16a5b", "92594b", "623d36"
    )
    private val personasHairVariants = listOf(
        "bald", "balding", "beanie", "bobBangs", "bobCut", "bunUndercut",
        "buzzcut", "cap", "curly", "curlyBun", "curlyHighTop", "extraLong",
        "fade", "long", "mohawk", "pigtails", "shortCombover",
        "shortComboverChops", "sideShave", "straightBun"
    )
    private val personasHairColors = listOf(
        "362c47", "f29c65", "6c4545", "f27d65", "dee1f5", "e16381", "e15c66"
    )
    val personasEyesVariants: List<String> = listOf(
        "happy", "open", "glasses", "sunglasses", "wink"
    )
    private val personasClothes = listOf("checkered", "rounded", "small", "squared")
    private val personasClothingColors = listOf(
        "456dff", "6dbb58", "f55d81", "7555ca", "e24553", "54d7c7", "f3b63a"
    )
    val personasMouths: List<String> = listOf("bigSmile", "smile", "lips", "smirk")
    private val personasNoses = listOf("mediumRound", "smallRound", "wrinkles")

    private val styleRules: Map<AvatarStyle, Map<String, Rule>> = mapOf(
        AvatarStyle.OPEN_PEEPS to mapOf(
            "skinColor" to Rule.ColorPalette(openPeepsSkinColors.toSet()),
            "headVariant" to Rule.EnumValues(openPeepsHeadVariants.toSet()),
            "headProbability" to Rule.Probability,
            "headContrastColor" to Rule.ColorPalette(openPeepsHeadContrastColors.toSet()),
            "expressionVariant" to Rule.EnumValues(openPeepsExpressions.toSet()),
            "expressionProbability" to Rule.Probability,
            "clothingColor" to Rule.ColorPalette(openPeepsClothingColors.toSet()),
            "accessoriesVariant" to Rule.EnumValues(openPeepsAccessories.toSet()),
            "accessoriesProbability" to Rule.Probability,
            "backgroundColor" to Rule.ColorPalette(BACKGROUND_COLORS.toSet())
        ),
        AvatarStyle.MICAH to mapOf(
            "baseColor" to Rule.ColorPalette(micahSkinColors.toSet()),
            "hairVariant" to Rule.EnumValues(micahHairVariants.toSet()),
            "hairProbability" to Rule.Probability,
            "hairColor" to Rule.ColorPalette(micahAccentColors.toSet()),
            "eyesVariant" to Rule.EnumValues(micahEyesVariants.toSet()),
            "eyesProbability" to Rule.Probability,
            "eyebrowsVariant" to Rule.EnumValues(micahEyebrowsVariants.toSet()),
            "eyebrowsProbability" to Rule.Probability,
            "mouthVariant" to Rule.EnumValues(micahMouthVariants.toSet()),
            "mouthProbability" to Rule.Probability,
            "noseVariant" to Rule.EnumValues(micahNoseVariants.toSet()),
            "noseProbability" to Rule.Probability,
            "earsVariant" to Rule.EnumValues(micahEarsVariants.toSet()),
            "earsProbability" to Rule.Probability,
            "clothesVariant" to Rule.EnumValues(micahClothes.toSet()),
            "clothesProbability" to Rule.Probability,
            "shirtColor" to Rule.ColorPalette(micahAccentColors.toSet()),
            "glassesVariant" to Rule.EnumValues(micahGlasses.toSet()),
            "glassesProbability" to Rule.Probability,
            "glassesColor" to Rule.ColorPalette(micahAccentColors.toSet()),
            "earringsVariant" to Rule.EnumValues(micahEarrings.toSet()),
            "earringsProbability" to Rule.Probability,
            "earringColor" to Rule.ColorPalette(micahAccentColors.toSet()),
            "eyeShadowColor" to Rule.ColorPalette(micahEyeShadowColors.toSet()),
            "backgroundColor" to Rule.ColorPalette(BACKGROUND_COLORS.toSet())
        ),
        AvatarStyle.PERSONAS to mapOf(
            "skinColor" to Rule.ColorPalette(personasSkinColors.toSet()),
            "hairVariant" to Rule.EnumValues(personasHairVariants.toSet()),
            "hairProbability" to Rule.Probability,
            "hairColor" to Rule.ColorPalette(personasHairColors.toSet()),
            "eyesVariant" to Rule.EnumValues(personasEyesVariants.toSet()),
            "eyesProbability" to Rule.Probability,
            "clothesVariant" to Rule.EnumValues(personasClothes.toSet()),
            "clothesProbability" to Rule.Probability,
            "clothingColor" to Rule.ColorPalette(personasClothingColors.toSet()),
            "mouthVariant" to Rule.EnumValues(personasMouths.toSet()),
            "mouthProbability" to Rule.Probability,
            "noseVariant" to Rule.EnumValues(personasNoses.toSet()),
            "noseProbability" to Rule.Probability,
            "backgroundColor" to Rule.ColorPalette(BACKGROUND_COLORS.toSet())
        )
    )

    /**
     * 12 base grid presets per style, explicitly pinning friendly-only expressions, mouths,
     * eyes, eyebrows, and accessories with zero facial hair options.
     */
    val BASE_PRESETS_BY_STYLE: Map<AvatarStyle, List<AvatarCode>> = mapOf(
        AvatarStyle.OPEN_PEEPS to BASE_SEEDS_BY_STYLE.getValue(AvatarStyle.OPEN_PEEPS).mapIndexed { idx, seed ->
            val opts = sortedMapOf(
                "expressionVariant" to openPeepsExpressions[idx % openPeepsExpressions.size]
            )
            if (idx % 4 == 1) {
                opts["accessoriesProbability"] = "100"
                opts["accessoriesVariant"] = openPeepsAccessories[idx % openPeepsAccessories.size]
            } else {
                opts["accessoriesProbability"] = "0"
            }
            AvatarCode(style = AvatarStyle.OPEN_PEEPS.id, seed = seed, options = opts)
        },
        AvatarStyle.MICAH to BASE_SEEDS_BY_STYLE.getValue(AvatarStyle.MICAH).mapIndexed { idx, seed ->
            val opts = sortedMapOf(
                "mouthVariant" to micahMouthVariants[idx % micahMouthVariants.size],
                "eyesVariant" to micahEyesVariants[idx % micahEyesVariants.size],
                "eyebrowsVariant" to micahEyebrowsVariants[idx % micahEyebrowsVariants.size]
            )
            AvatarCode(style = AvatarStyle.MICAH.id, seed = seed, options = opts)
        },
        AvatarStyle.PERSONAS to BASE_SEEDS_BY_STYLE.getValue(AvatarStyle.PERSONAS).mapIndexed { idx, seed ->
            val opts = sortedMapOf(
                "mouthVariant" to personasMouths[idx % personasMouths.size],
                "eyesVariant" to personasEyesVariants[idx % personasEyesVariants.size]
            )
            AvatarCode(style = AvatarStyle.PERSONAS.id, seed = seed, options = opts)
        }
    )

    fun getBasePresets(style: AvatarStyle): List<AvatarCode> =
        BASE_PRESETS_BY_STYLE[style].orEmpty()

    fun getBasePreset(style: AvatarStyle, seed: String): AvatarCode? =
        BASE_PRESETS_BY_STYLE[style]?.firstOrNull { it.seed == seed }

    fun defaultFriendlyOptions(style: AvatarStyle): Map<String, String> = when (style) {
        AvatarStyle.OPEN_PEEPS -> sortedMapOf(
            "accessoriesProbability" to "0",
            "expressionVariant" to "smile"
        )
        AvatarStyle.MICAH -> sortedMapOf(
            "eyebrowsVariant" to "up",
            "eyesVariant" to "smiling",
            "glassesProbability" to "0",
            "mouthVariant" to "smile"
        )
        AvatarStyle.PERSONAS -> sortedMapOf(
            "eyesVariant" to "open",
            "mouthVariant" to "smile"
        )
    }

    /**
     * Returns only the customization rows that [style] supports (no facial hair row in any style).
     */
    fun getSupportedRows(style: AvatarStyle): List<AvatarOptionRow> = when (style) {
        AvatarStyle.OPEN_PEEPS -> listOf(
            AvatarOptionRow.ColorSwatches("Skin", "skinColor", openPeepsSkinColors),
            AvatarOptionRow.VariantChips("Hair", "headVariant", openPeepsHeadVariants),
            AvatarOptionRow.VariantChips("Expression", "expressionVariant", openPeepsExpressions),
            AvatarOptionRow.ColorSwatches("Clothes Color", "clothingColor", openPeepsClothingColors),
            AvatarOptionRow.OptionalVariantChips(
                title = "Accessories",
                variantKey = "accessoriesVariant",
                probabilityKey = "accessoriesProbability",
                values = openPeepsAccessories
            ),
            AvatarOptionRow.ColorSwatches("Background Color", "backgroundColor", BACKGROUND_COLORS)
        )

        AvatarStyle.MICAH -> listOf(
            AvatarOptionRow.ColorSwatches("Skin", "baseColor", micahSkinColors),
            AvatarOptionRow.VariantChips("Hair", "hairVariant", micahHairVariants),
            AvatarOptionRow.ColorSwatches("Hair Color", "hairColor", micahAccentColors),
            AvatarOptionRow.VariantChips("Eyes", "eyesVariant", micahEyesVariants),
            AvatarOptionRow.VariantChips("Eyebrows", "eyebrowsVariant", micahEyebrowsVariants),
            AvatarOptionRow.VariantChips("Mouth", "mouthVariant", micahMouthVariants),
            AvatarOptionRow.VariantChips("Clothes", "clothesVariant", micahClothes),
            AvatarOptionRow.ColorSwatches("Clothes Color", "shirtColor", micahAccentColors),
            AvatarOptionRow.OptionalVariantChips(
                title = "Accessories",
                variantKey = "glassesVariant",
                probabilityKey = "glassesProbability",
                values = micahGlasses
            ),
            AvatarOptionRow.ColorSwatches("Background Color", "backgroundColor", BACKGROUND_COLORS)
        )

        AvatarStyle.PERSONAS -> listOf(
            AvatarOptionRow.ColorSwatches("Skin", "skinColor", personasSkinColors),
            AvatarOptionRow.VariantChips("Hair", "hairVariant", personasHairVariants),
            AvatarOptionRow.ColorSwatches("Hair Color", "hairColor", personasHairColors),
            AvatarOptionRow.VariantChips("Eyes", "eyesVariant", personasEyesVariants),
            AvatarOptionRow.VariantChips("Mouth", "mouthVariant", personasMouths),
            AvatarOptionRow.VariantChips("Clothes", "clothesVariant", personasClothes),
            AvatarOptionRow.ColorSwatches("Clothes Color", "clothingColor", personasClothingColors),
            AvatarOptionRow.ColorSwatches("Background Color", "backgroundColor", BACKGROUND_COLORS)
        )
    }

    fun generateRandomSeed(random: Random = secureRandom): String {
        val sb = StringBuilder("av_")
        repeat(12) {
            sb.append(ALNUM_CHARS[random.nextInt(ALNUM_CHARS.length)])
        }
        return sb.toString()
    }

    fun isValidStyle(style: String?): Boolean = AvatarStyle.fromId(style) != null

    fun isValidSeed(seed: String?): Boolean =
        seed != null && SEED_REGEX.matches(seed)

    fun isOptionAllowed(style: AvatarStyle, key: String, value: String): Boolean {
        val rule = styleRules[style]?.get(key) ?: return false
        return when (rule) {
            is Rule.EnumValues -> value in rule.values
            is Rule.ColorPalette -> {
                val lower = value.lowercase(Locale.US)
                (lower == "transparent" || HEX_6_REGEX.matches(lower)) && lower in rule.values
            }
            is Rule.Probability -> {
                if (!PROB_REGEX.matches(value)) return false
                val n = value.toIntOrNull() ?: return false
                n in 0..100
            }
        }
    }

    /**
     * Filters and normalizes [rawOptions] against [style]'s friendly-only whitelist.
     * Any legacy facial hair or blocked values on an existing user's avatar are gracefully
     * stripped or mapped to the nearest allowed friendly equivalent without crashing.
     */
    fun sanitizeOptions(style: AvatarStyle, rawOptions: Map<String, String>?): Map<String, String> {
        if (rawOptions.isNullOrEmpty()) return emptyMap()
        val rules = styleRules[style] ?: return emptyMap()
        val result = sortedMapOf<String, String>()
        var hadBlockedOpenPeepsAccessory = false

        for ((k, rawV) in rawOptions) {
            val v = rawV.trim()

            // Remove all facial hair options completely on read/parse
            if (k.startsWith("facialHair")) {
                continue
            }

            // Remove mask options completely
            if (style == AvatarStyle.OPEN_PEEPS && (k == "maskVariant" || k == "maskProbability")) {
                continue
            }

            // Nearest-allowed fallback for blocked accessories (e.g. eyepatch)
            if (style == AvatarStyle.OPEN_PEEPS && k == "accessoriesVariant") {
                if (!isOptionAllowed(style, k, v)) {
                    hadBlockedOpenPeepsAccessory = true
                    continue
                }
            }

            // Nearest-allowed fallback for blocked expressions / mouths / eyebrows / eyes
            if (k == "expressionVariant" && style == AvatarStyle.OPEN_PEEPS) {
                result[k] = if (isOptionAllowed(style, k, v)) v else "smile"
                continue
            }
            if (k == "mouthVariant" && (style == AvatarStyle.MICAH || style == AvatarStyle.PERSONAS)) {
                result[k] = if (isOptionAllowed(style, k, v)) v else "smile"
                continue
            }
            if (k == "eyebrowsVariant" && style == AvatarStyle.MICAH) {
                result[k] = when {
                    isOptionAllowed(style, k, v) -> v
                    v == "eyelashesDown" -> "eyelashesUp"
                    else -> "up"
                }
                continue
            }
            if (k == "eyesVariant" && style == AvatarStyle.MICAH) {
                result[k] = if (isOptionAllowed(style, k, v)) v else "smiling"
                continue
            }
            if (k == "eyesVariant" && style == AvatarStyle.PERSONAS) {
                result[k] = if (isOptionAllowed(style, k, v)) v else "open"
                continue
            }

            val rule = rules[k] ?: continue
            val normalizedVal = when (rule) {
                is Rule.ColorPalette -> v.lowercase(Locale.US)
                is Rule.Probability -> v
                is Rule.EnumValues -> v
            }
            if (isOptionAllowed(style, k, normalizedVal)) {
                result[k] = normalizedVal
            }
        }

        if (hadBlockedOpenPeepsAccessory) {
            result.remove("accessoriesVariant")
            result["accessoriesProbability"] = "0"
        }

        if (gson.toJson(result).toByteArray(Charsets.UTF_8).size > MAX_OPTIONS_JSON_BYTES) {
            return emptyMap()
        }
        return result
    }

    /**
     * Strictly validates [rawOptions]: returns true only if every key/value is whitelisted
     * for [style] and the serialized JSON is <= 1024 bytes.
     */
    fun areOptionsStrictlyValid(style: AvatarStyle, rawOptions: Map<String, String>?): Boolean {
        if (rawOptions.isNullOrEmpty()) return true
        for ((k, v) in rawOptions) {
            val normalized = if (styleRules[style]?.get(k) is Rule.ColorPalette) {
                v.trim().lowercase(Locale.US)
            } else {
                v.trim()
            }
            if (!isOptionAllowed(style, k, normalized)) return false
        }
        val bytes = gson.toJson(rawOptions.toSortedMap()).toByteArray(Charsets.UTF_8).size
        return bytes <= MAX_OPTIONS_JSON_BYTES
    }

    /**
     * Parses and sanitizes an [AvatarCode]. Returns null if [raw] is null,
     * has an unknown style, or has an invalid seed.
     */
    fun parseAvatarCode(raw: AvatarCode?): AvatarCode? {
        if (raw == null) return null
        val style = AvatarStyle.fromId(raw.style) ?: return null
        if (!isValidSeed(raw.seed)) return null
        val cleanedOptions = sanitizeOptions(style, raw.options)
        return AvatarCode(
            style = style.id,
            seed = raw.seed,
            options = cleanedOptions
        )
    }

    /**
     * Parses raw JSON into a validated [AvatarCode], or returns null on null/malformed/invalid data.
     */
    fun parseAvatarJson(json: String?): AvatarCode? {
        if (json.isNullOrBlank()) return null
        return try {
            val root = gson.fromJson(json, Map::class.java) ?: return null
            val styleStr = root["style"] as? String ?: return null
            val seedStr = root["seed"] as? String ?: return null
            val style = AvatarStyle.fromId(styleStr) ?: return null
            if (!isValidSeed(seedStr)) return null

            val rawOptionsMap = mutableMapOf<String, String>()
            val optsRaw = root["options"]
            if (optsRaw != null) {
                if (optsRaw !is Map<*, *>) return null
                for ((k, v) in optsRaw) {
                    if (k is String && v != null) {
                        when (v) {
                            is String -> rawOptionsMap[k] = v
                            is Number -> {
                                val d = v.toDouble()
                                rawOptionsMap[k] = if (d % 1.0 == 0.0) d.toLong().toString() else v.toString()
                            }
                            else -> return null
                        }
                    }
                }
            }
            AvatarCode(
                style = style.id,
                seed = seedStr,
                options = sanitizeOptions(style, rawOptionsMap)
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun toJson(avatar: AvatarCode?): String? {
        val parsed = parseAvatarCode(avatar) ?: return null
        return gson.toJson(parsed)
    }

    /**
     * Generates a randomized [AvatarCode] using a new random seed and strictly
     * friendly-only options (never facial hair) for [style].
     */
    fun randomize(style: AvatarStyle, random: Random = secureRandom): AvatarCode {
        val options = sortedMapOf<String, String>()
        for (row in getSupportedRows(style)) {
            when (row) {
                is AvatarOptionRow.ColorSwatches -> {
                    val palette = row.colors.filter { it != "transparent" }
                    if (palette.isNotEmpty()) {
                        options[row.optionKey] = palette[random.nextInt(palette.size)]
                    }
                }
                is AvatarOptionRow.VariantChips -> {
                    if (row.values.isNotEmpty()) {
                        options[row.optionKey] = row.values[random.nextInt(row.values.size)]
                    }
                }
                is AvatarOptionRow.OptionalVariantChips -> {
                    val includeFeature = random.nextBoolean()
                    if (includeFeature && row.values.isNotEmpty()) {
                        options[row.probabilityKey] = "100"
                        options[row.variantKey] = row.values[random.nextInt(row.values.size)]
                    } else {
                        options[row.probabilityKey] = "0"
                    }
                }
            }
        }
        return AvatarCode(
            style = style.id,
            seed = generateRandomSeed(random),
            options = options
        )
    }

    /**
     * Formats camelCase/alnum option value into a concise user-facing label.
     */
    fun formatOptionLabel(raw: String): String {
        if (raw.isBlank()) return "Default"
        return raw
            .replace(Regex("([a-z])([A-Z0-9])"), "$1 $2")
            .replace(Regex("([0-9])([A-Z])"), "$1 $2")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    /**
     * Extracts clean 1-2 character uppercase fallback initials for the letter badge.
     */
    fun extractInitials(displayName: String?): String {
        val cleaned = displayName.orEmpty().trim()
        if (cleaned.isEmpty()) return "U"
        return cleaned.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
    }
}
