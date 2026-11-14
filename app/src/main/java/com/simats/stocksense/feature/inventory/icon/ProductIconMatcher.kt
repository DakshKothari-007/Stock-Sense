package com.simats.stocksense.feature.inventory.icon

import kotlin.math.min

/**
 * Result of matching a product name against the [ProductIconCatalog].
 */
data class ProductIconMatchResult(
    val iconKey: String,
    val confidence: Float,
    val item: ProductIconItem
)

/**
 * Intelligent product icon matcher for supermarket retail products.
 *
 * Matching algorithm:
 * 1. Normalize product name: lowercase, strip quantities/units (e.g. 500g, 1 kg, ml, pack, etc.),
 *    strip numbers, remove punctuation and symbols.
 * 2. Exact multi-word phrase matching with specificity scoring (e.g., "coconut oil" gets oil).
 * 3. Exact word token matching against keywords in English, Hindi, Tamil, Hinglish and Tanglish.
 * 4. Plural and word stemming (e.g., "apples" -> "apple", "biscuits" -> "biscuit").
 * 5. Typo tolerance with Levenshtein distance 1 for words of 5+ letters.
 * 6. Brand and partial keyword scoring.
 * 7. Category / department affinity boost.
 */
object ProductIconMatcher {

    const val DEFAULT_THRESHOLD = 0.50f

    private val UNIT_REGEX = Regex(
        "\\b\\d+(\\.\\d+)?\\s*(g|gm|gms|gram|grams|kg|kgs|kilo|kilos|ml|l|ltr|ltrs|liter|liters|litre|litres|pc|pcs|piece|pieces|pack|packs|pk|pkt|pkts|bundle|combo|family\\s*pack|super\\s*saver|pouch|can|tin|bottle|box|sachet|unit|units|doz|dozen|bunch|meter|m|cm|mm|pages|page|w|watt|watts|v|volt|volts|mg|tablet|tablets|capsule|capsules)\\b",
        RegexOption.IGNORE_CASE
    )

    private val PACKAGING_WORDS_REGEX = Regex(
        "\\b(family\\s*pack|super\\s*saver|pack|packs|pkt|pkts|combo|pouch|sachet|bottle|tin|can|box|bundle)\\b",
        RegexOption.IGNORE_CASE
    )

    private val NUM_REGEX = Regex("\\b\\d+\\b")

    private val SYMBOLS_REGEX = Regex("[\\-_/\\\\·,.:;!|~`#\'\"()\\[\\]{}+*?@$%^&<>=]")

    private val MULTI_SPACE_REGEX = Regex("\\s+")

    private val GENERIC_MODIFIERS = setOf(
        "green", "red", "white", "black", "blue", "yellow", "orange",
        "fresh", "pure", "super", "hot", "cold", "sweet", "extra", "natural", "organic", "dry"
    )

    /**
     * Normalizes a product name by removing units, numbers, and symbols.
     * Never throws on empty or null inputs.
     */
    fun normalize(name: String?): String {
        if (name.isNullOrBlank()) return ""
        var s = name.lowercase().trim()
        s = UNIT_REGEX.replace(s, " ")
        s = PACKAGING_WORDS_REGEX.replace(s, " ")
        s = SYMBOLS_REGEX.replace(s, " ")
        s = NUM_REGEX.replace(s, " ")
        return MULTI_SPACE_REGEX.replace(s, " ").trim()
    }

    /**
     * Computes Levenshtein edit distance between two strings.
     */
    fun levenshtein(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val prev = IntArray(s2.length + 1) { it }
        val curr = IntArray(s2.length + 1)

        for (i in s1.indices) {
            curr[0] = i + 1
            val c1 = s1[i]
            for (j in s2.indices) {
                val c2 = s2[j]
                val cost = if (c1 == c2) 0 else 1
                curr[j + 1] = min(
                    min(curr[j] + 1, prev[j + 1] + 1),
                    prev[j] + cost
                )
            }
            System.arraycopy(curr, 0, prev, 0, prev.size)
        }
        return prev[s2.length]
    }

    /**
     * Reduces common English plurals for fuzzy matching.
     */
    private fun stemWord(w: String): String {
        if (w.length > 4) {
            if (w.endsWith("ies")) return w.substring(0, w.length - 3) + "y"
            if (w.endsWith("es") && !w.endsWith("tes")) return w.substring(0, w.length - 2)
            if (w.endsWith("s") && !w.endsWith("ss")) return w.substring(0, w.length - 1)
        }
        return w
    }

    /**
     * Matches a product name and returns the best [ProductIconMatchResult],
     * or null if confidence is below [threshold].
     */
    fun match(
        name: String?,
        category: String? = null,
        threshold: Float = DEFAULT_THRESHOLD
    ): ProductIconMatchResult? {
        val norm = normalize(name)
        if (norm.isEmpty()) return null

        val tokens = norm.split(' ').filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null

        val catDept = if (!category.isNullOrBlank()) ProductDepartment.fromCategory(category) else null

        var bestItem: ProductIconItem? = null
        var bestScore = 0.0f

        for (item in ProductIconCatalog.ITEMS) {
            var itemScore = 0.0f

            for (kw in item.keywords) {
                val kwClean = kw.trim().lowercase()
                if (kwClean.isEmpty()) continue

                if (kwClean.contains(' ')) {
                    // Multi-word phrase match in normalized text
                    // Longer phrases get a specificity bonus ("coconut oil" beats "coconut")
                    if (norm == kwClean || norm.contains(" $kwClean ") || norm.startsWith("$kwClean ") || norm.endsWith(" $kwClean")) {
                        val score = 0.95f + min(kwClean.length * 0.005f, 0.04f)
                        if (score > itemScore) itemScore = score
                    }
                } else {
                    // Single word token matching
                    for (t in tokens) {
                        if (t == kwClean) {
                            val base = if (kwClean in GENERIC_MODIFIERS) 0.65f else 0.90f
                            val score = base + min(kwClean.length * 0.002f, 0.03f)
                            if (score > itemScore) itemScore = score
                        } else if (stemWord(t) == stemWord(kwClean)) {
                            val base = if (kwClean in GENERIC_MODIFIERS) 0.60f else 0.82f
                            val score = base
                            if (score > itemScore) itemScore = score
                        } else if (t.length >= 5 && kwClean.length >= 5 && levenshtein(t, kwClean) == 1) {
                            val base = if (kwClean in GENERIC_MODIFIERS) 0.50f else 0.72f
                            val score = base
                            if (score > itemScore) itemScore = score
                        }
                    }
                }
            }

            // Department alignment bonus
            if (catDept != null && item.department == catDept && itemScore > 0.0f) {
                itemScore += 0.05f
            }

            if (itemScore > bestScore) {
                bestScore = itemScore
                bestItem = item
            }
        }

        val clampedConfidence = min(bestScore, 1.0f)
        return if (bestItem != null && clampedConfidence >= threshold) {
            ProductIconMatchResult(
                iconKey = bestItem.iconKey,
                confidence = clampedConfidence,
                item = bestItem
            )
        } else {
            null
        }
    }

    /**
     * Resolves the icon drawable following the strict fallback hierarchy:
     * 1. Saved icon_key from server
     * 2. Auto-matcher result (if confidence >= threshold)
     * 3. Department icon from category
     * 4. Generic package icon
     * Never crashes on null or empty input.
     */
    fun resolveDrawableRes(
        name: String?,
        category: String? = null,
        savedIconKey: String? = null
    ): Int {
        // 1. Saved icon_key from server
        if (!savedIconKey.isNullOrBlank()) {
            val item = ProductIconCatalog.get(savedIconKey)
            if (item != null) return item.drawableRes
            try {
                android.util.Log.w("ProductAvatar", "Saved icon key '$savedIconKey' not found in catalog for product '$name'")
            } catch (_: Throwable) {
                // Safe in JVM unit tests
            }
        }

        // 2. Matcher
        val matchResult = match(name, category)
        if (matchResult != null) {
            return matchResult.item.drawableRes
        }

        // 3. Department icon from category
        if (!category.isNullOrBlank()) {
            return ProductIconCatalog.getDepartmentIcon(category)
        }

        // 4. Generic package icon
        return ProductIconCatalog.DEFAULT_PACKAGE_ICON
    }

    /**
     * Resolves the [ProductDepartment] for soft background tinting.
     */
    fun resolveDepartment(
        name: String?,
        category: String? = null,
        savedIconKey: String? = null
    ): ProductDepartment {
        if (!savedIconKey.isNullOrBlank()) {
            val item = ProductIconCatalog.get(savedIconKey)
            if (item != null) return item.department
        }
        val matchResult = match(name, category)
        if (matchResult != null) {
            return matchResult.item.department
        }
        return ProductDepartment.fromCategory(category)
    }
}

